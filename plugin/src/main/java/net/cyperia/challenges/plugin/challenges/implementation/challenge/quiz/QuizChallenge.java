package net.cyperia.challenges.plugin.challenges.implementation.challenge.quiz;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.command.CommandProvider;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.spigot.events.PlayerJumpEvent;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Records what every player does (blocks walked, mobs killed, damage taken, ...) and every few minutes
 * quizzes a random player about their own statistics. Getting it wrong costs a heart of maximum health.
 * <p>
 * All state lives on the main thread except {@link #activeQuestion}, which the (asynchronous) bossbar
 * update reads and is therefore swapped as a whole instead of being mutated field by field.
 */
public class QuizChallenge extends TimedChallenge implements CommandProvider {

  private static final int ANSWER_TIME_SECONDS = 60;

  /** Number of options a question presents, the right answer included. */
  private static final int OPTION_COUNT = 4;

  /**
   * Smallest distance a wrong number may have from the right one, so decoys stay plausible for
   * small statistics without becoming guessable for large ones.
   */
  private static final int MIN_DECOY_SPREAD = 5;

  /** Random draws spent on generating distinct decoys before falling back to counting upwards. */
  private static final int MAX_DECOY_DRAWS = 32;

  /** Health points (half hearts) of maximum health a wrong answer costs */
  private static final int HEALTH_PENALTY = 2;

  private static QuizChallenge instance;

  private final IRandom random = IRandom.create();
  private final Set<QuestionFactory> seenQuestions = new HashSet<>();

  private volatile ActiveQuestion activeQuestion; // null while waiting for the next activation

  public QuizChallenge() {
    super(1, 20, 5, false);
    instance = this;
    setValueUnit(ValueUnit.INTERVAL_MINUTES.scaled(3).jitter(60));
  }

  @NotNull
  private static QuizChallenge instance() {
    QuizChallenge current = instance;
    if (current == null) throw new IllegalStateException("QuizChallenge has not been constructed yet");
    return current;
  }

  @NotNull
  private static MessageKey messageKey(@NotNull String keySuffix) {
    return instance().getChallengeMessageKey(keySuffix);
  }

  @NotNull
  @Override
  protected IRandom getRandom() {
    return random;
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      ActiveQuestion active = activeQuestion; // single read, the field may change concurrently
      if (active == null) {
        bossbar.setColor(BossBar.Color.YELLOW);
        bossbar.setTitle(getChallengeMessageKey("bossbar.waiting"));
        return;
      }
      LocalizableMessage time = ChallengeAPI.formatTime(active.getSecondsLeft());
      bossbar.setProgress((float) active.getSecondsLeft() / ANSWER_TIME_SECONDS);
      if (active.getPlayer() != player) {
        bossbar.setColor(BossBar.Color.GREEN);
        bossbar.setTitle(getChallengeMessageKey("bossbar.question-other"), time, active.getPlayer());
        return;
      }
      bossbar.setColor(BossBar.Color.RED);
      bossbar.setTitle(getChallengeMessageKey("bossbar.question"), time);
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    // the countdown is stopped while a question is open, so dropping one here has to restart it,
    // otherwise re-enabling the challenge would never ask another question
    if (activeQuestion != null) {
      activeQuestion = null;
      restartTimer();
    }
    bossbar.hide();
  }

  // Question lifecycle

  @Override
  protected void onTimeActivation() {
    List<Player> ingamePlayers = ChallengeHelper.getIngamePlayers();
    if (ingamePlayers.isEmpty()) {
      restartTimer(5); // nobody to ask right now, try again later
      return;
    }

    Player player = random.choose(ingamePlayers);
    Question question = createQuestion(player);
    if (question == null) {
      restartTimer(1); // the player has not collected any statistic we could ask about yet
      seenQuestions.clear();
      return;
    }

    activeQuestion = new ActiveQuestion(player, question, ANSWER_TIME_SECONDS);
    sendQuestion(player, question);
    SoundSample.PLOP.play(player);
    bossbar.update();
  }

  /**
   * Tries every statistic in random order and returns the first question the player has enough data
   * for, or {@code null} if there is none.
   */
  @Nullable
  private Question createQuestion(@NotNull Player player) {
    List<SavedStatistic> statistics = new ArrayList<>(SavedStatistic.values());
    random.shuffle(statistics);

    for (SavedStatistic statistic : statistics) {
      List<QuestionFactory> factories = new ArrayList<>(statistic.getQuestionFactories());
      random.shuffle(factories);

      for (QuestionFactory factory : factories) {
        if (seenQuestions.contains(factory)) continue;
        Question question = factory.create(player, random);
        if (question != null) {
          seenQuestions.add(factory);
          return question;
        }
      }
    }
    return null;
  }

  private void sendQuestion(@NotNull Player player, @NotNull Question question) {
    MessageKey option = messageKey("question.option");
    // {0} is the key the click event passes to /guess and is embedded as plain text, {1} is the
    // translated display value
    List<LocalizableMessage> options = question.options().stream()
      .<LocalizableMessage>map(answer -> option.withArgs(answer.key(), answer.display()))
      .toList();

    messageKey("question.format").send(player, Prefix.QUIZ, question.prompt(), options);
  }

  @ScheduledTask(ticks = 20, async = false)
  public void handleQuestionSecond() {
    ActiveQuestion active = activeQuestion;
    if (active == null) return;

    Player player = active.getPlayer();
    if (!player.isOnline() || ignorePlayer(player)) {
      // the player cannot answer anymore through no fault of their own, so don't punish them
      messageKey("cancelled").broadcast(Prefix.QUIZ);
      endQuestion();
      return;
    }

    if (active.getSecondsLeft() <= 1) {
      messageKey("time-up").broadcast(Prefix.QUIZ);
      revealSolution(player, active.getQuestion());
      Regions.entity(plugin, player, () -> punish(player));
      endQuestion();
      return;
    }

    active.tick();
    bossbar.update();
  }

  private void endQuestion() {
    activeQuestion = null;
    restartTimer();
    bossbar.update();
  }

  private void revealSolution(@NotNull Player player, @NotNull Question question) {
    List<Object> solutions = question.correctAnswers().stream().map(Answer::display).toList();
    MessageKey.pluralize(messageKey("answer.solution").getKey(), solutions.size())
      .send(player, Prefix.QUIZ, LocalizableMessage.joinList(solutions));
    SoundSample.BREAK.play(player);
  }

  /**
   * Takes one heart of maximum health. Once that would leave the player with less than a full heart
   * their maximum health is restored and they are killed instead.
   */
  private void punish(@NotNull Player player) {
    AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) return;

    double reduced = attribute.getBaseValue() - HEALTH_PENALTY;
    if (reduced < HEALTH_PENALTY) {
      attribute.setBaseValue(attribute.getDefaultValue());
      ChallengeHelper.kill(player);
      return;
    }
    attribute.setBaseValue(reduced);
  }

  // Command

  @NotNull
  @Override
  public List<ChallengeCommand> getCommands() {
    return List.of(new GuessCommand());
  }

  private void guess(@NotNull Player player, @NotNull String input) throws CommandSyntaxException {
    ActiveQuestion active = activeQuestion;
    if (active == null || active.getPlayer() != player) {
      messageKey("command.none").send(player, Prefix.QUIZ);
      SoundSample.BASS_OFF.play(player);
      return;
    }

    Question question = active.getQuestion();
    Answer answer = question.findOption(input);
    if (answer == null) {
      // options are clicked, not typed: anything else is a misuse and must not count as a wrong answer
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
    }

    if (!question.correctAnswers().contains(answer)) {
      broadcastAnswer(player, "wrong", answer);
      revealSolution(player, question);
      punish(player);
      endQuestion();
      return;
    }

    broadcastAnswer(player, "right", answer);
    SoundSample.LEVEL_UP.play(player);
    endQuestion();
  }

  private void broadcastAnswer(@NotNull Player player, @NotNull String answerKeySuffix, @NotNull Answer answer) {
    MessageKey answeredMessage = messageKey("answer." + answerKeySuffix);
    MessageKey othersMessage = messageKey("answer." + answerKeySuffix + "-other");

    broadcastAudience(target -> {
      if (target == player) {
        answeredMessage.send(target, Prefix.QUIZ, answer.display());
      } else {
        othersMessage.send(target, Prefix.QUIZ, player);
      }
    });
  }

  private class GuessCommand implements ChallengeCommand {

    @NotNull
    @Override
    public CommandInfo info() {
      return CommandInfo.command("guess", ChallengesPermissions.COMMAND_GUESS);
    }

    @Override
    public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      root.then(nodes.argument("answer", StringArgumentType.word())
        .suggests(nodes.suggest(sender -> {
          ActiveQuestion active = activeQuestion;
          if (active == null || active.getPlayer() != sender) return List.of();
          return active.getQuestion().options().stream().map(Answer::key).toList();
        }))
        .executes(nodes.runAsPlayer((player, context) -> guess(player, StringArgumentType.getString(context, "answer")))));
    }

  }

  // Model

  @Getter
  @AllArgsConstructor
  private class ActiveQuestion {

    private final Player player;
    private final Question question;
    private int secondsLeft;

    public void tick() {
      secondsLeft--;
    }
  }

  /**
   * A generated question: what is asked, which options are shown and which of them count as correct.
   * More than one option may be correct when several statistics are tied.
   */
  public record Question(@NotNull LocalizableMessage prompt, @NotNull List<Answer> options,
                         @NotNull List<Answer> correctAnswers) {

    @Nullable
    public Answer findOption(@NotNull String key) {
      for (Answer option : options) {
        if (option.key().equals(key)) return option;
      }
      return null;
    }

  }

  /**
   * One selectable option of a {@link Question}.
   *
   * @param key     identifies the option within its question and is what the option's click event
   *                passes to {@code /guess}. Always plain ascii, which makes it safe to embed into a
   *                message before it is parsed.
   * @param display the value shown to the player; translated by the message pipeline, so pass the
   *                value itself (e.g. an {@link EntityType}) rather than its name
   */
  public record Answer(@NotNull String key, @NotNull Object display) {

    @NotNull
    public static Answer of(int value) {
      return new Answer(Integer.toString(value), value);
    }

    @NotNull
    public static Answer of(@NotNull Enum<?> value) {
      return new Answer(value.name().toLowerCase(Locale.ROOT), value);
    }

  }

  /**
   * Creates a concrete {@link Question} for a player, or {@code null} if they have no data for it.
   */
  @FunctionalInterface
  public interface QuestionFactory {

    @Nullable
    Question create(@NotNull Player player, @NotNull IRandom random);

  }

  // Statistics

  /**
   * A value tracked per player that questions can be generated from.
   *
   * @implNote every statistic is stored below its own key in the challenge's player gamestate data,
   * so enabling the challenge later never invalidates what was already collected.
   */
  public interface SavedStatistic {

    IntegerStatistic BLOCKS_MOVED = new IntegerStatistic("blocks-traveled", "traveled", "far", "be");
    IntegerStatistic JUMPED = new IntegerStatistic("jumped", "jumped", "often", "be");
    IntegerStatistic SNEAKED = new IntegerStatistic("sneaked", "sneaked", "often", "be");
    DocumentCountStatistic<EntityType> MOB_KILLED = new DocumentCountStatistic<>("mob-killed", EntityType.class, "killed", "often", "have");
    DocumentCountStatistic<EntityType> MOB_DAMAGED = new DocumentCountStatistic<>("mobs-damaged", EntityType.class, "damaged", "damage", "have");
    DocumentCountStatistic<Material> BLOCKS_PLACED = new DocumentCountStatistic<>("blocks-placed", Material.class, "placed", "often", "have");
    DocumentCountStatistic<Material> BLOCKS_DESTROYED = new DocumentCountStatistic<>("blocks-destroyed", Material.class, "broken", "often", "have");
    DocumentCountStatistic<Material> ITEMS_DROPPED = new DocumentCountStatistic<>("items-dropped", Material.class, "dropped", "often", "have");
    DocumentCountStatistic<DamageCause> DAMAGE_TAKEN = new DocumentCountStatistic<>("damage-taken", DamageCause.class, "suffered", "damage-taken", "have");

    List<SavedStatistic> VALUES = List.of(BLOCKS_MOVED, JUMPED, SNEAKED, MOB_DAMAGED, MOB_KILLED,
      BLOCKS_PLACED, BLOCKS_DESTROYED, ITEMS_DROPPED, DAMAGE_TAKEN);

    @NotNull
    static List<SavedStatistic> values() {
      return VALUES;
    }

    /**
     * @return the gamestate key this statistic is stored below
     */
    @NotNull
    String getKey();

    @NotNull
    List<QuestionFactory> getQuestionFactories();

    /**
     * @return the verb describing this statistic, inflected to fit {@link #getCountQuestionMessage()},
     * e.g. {@code "walk"} for {@code "How far did you walk?"}
     */
    @NotNull
    MessageKey getVerbMessage();

    /**
     * @return the sentence asking for a concrete amount of this statistic
     */
    @NotNull
    MessageKey getCountQuestionMessage();

    /**
     * A statistic that is a single number, e.g. how many blocks a player has walked.
     */
    class IntegerStatistic implements SavedStatistic {

      private final String key;
      private final String verb;
      private final String questionSuffix;
      private final String questionVerb;
      private final List<QuestionFactory> questionFactories = List.of(this::createCountQuestion);

      public IntegerStatistic(@NotNull String key, @NotNull String verb, @NotNull String questionSuffix, @NotNull String questionVerb) {
        this.key = key;
        this.verb = verb;
        this.questionSuffix = questionSuffix;
        this.questionVerb = questionVerb;
      }

      @NotNull
      @Override
      public String getKey() {
        return key;
      }

      @NotNull
      @Override
      public List<QuestionFactory> getQuestionFactories() {
        return questionFactories;
      }

      @NotNull
      @Override
      public MessageKey getVerbMessage() {
        return messageKey("verb." + verb);
      }

      @NotNull
      @Override
      public MessageKey getCountQuestionMessage() {
        return messageKey("question." + questionSuffix + "-" + questionVerb);
      }

      @Nullable
      private Question createCountQuestion(@NotNull Player player, @NotNull IRandom random) {
        int rightAnswer = getStatistic(player);
        if (rightAnswer <= 0) return null; // nothing collected yet, the answer would be a giveaway

        List<Answer> options = createNumberOptions(rightAnswer, random);
        return new Question(getCountQuestionMessage().withArgs(getVerbMessage()), options, List.of(Answer.of(rightAnswer)));
      }

      public void increaseStatistic(@NotNull Player player) {
        increaseStatistic(player, 1);
      }

      public void increaseStatistic(@NotNull Player player, int amount) {
        instance().getPlayerData(player).set(key, getStatistic(player) + amount);
      }

      public int getStatistic(@NotNull Player player) {
        return instance().getPlayerData(player).getInt(key);
      }

    }

    /**
     * A statistic counted per value of an enum, e.g. how much damage a player took per
     * {@link DamageCause}. Keys are stored as the constant name, which makes them stable across
     * restarts and lets the value be translated when it is shown.
     *
     * @param <T> the enum the counted values belong to
     */
    class DocumentCountStatistic<T extends Enum<T>> implements SavedStatistic {

      private final String key;
      private final Class<T> valueType;
      private final String verb;
      private final String questionSuffix;
      private final String questionVerb;
      private final List<QuestionFactory> questionFactories = List.of(this::createMostQuestion, this::createCountQuestion);

      public DocumentCountStatistic(@NotNull String key, @NotNull Class<T> valueType, @NotNull String verb,
                                    @NotNull String questionSuffix, @NotNull String questionVerb) {
        this.key = key;
        this.valueType = valueType;
        this.verb = verb;
        this.questionSuffix = questionSuffix;
        this.questionVerb = questionVerb;
      }

      @NotNull
      @Override
      public String getKey() {
        return key;
      }

      @NotNull
      @Override
      public List<QuestionFactory> getQuestionFactories() {
        return questionFactories;
      }

      @NotNull
      @Override
      public MessageKey getVerbMessage() {
        return messageKey("verb." + verb);
      }

      @NotNull
      @Override
      public MessageKey getCountQuestionMessage() {
        return messageKey("question." + questionSuffix + "-" + questionVerb);
      }

      @NotNull
      public Document getDocument(@NotNull Player player) {
        return instance().getPlayerData(player).getDocument(key);
      }

      public void increaseStatistic(@NotNull Player player, @NotNull T value) {
        increaseStatistic(player, value, 1);
      }

      public void increaseStatistic(@NotNull Player player, @NotNull T value, double amount) {
        getDocument(player).set(value.name(), getStatistic(player, value) + amount);
      }

      public double getStatistic(@NotNull Player player, @NotNull T value) {
        return getDocument(player).getDouble(value.name());
      }

      /**
       * @return all values this player has collected, skipping constants a minecraft update removed
       */
      @NotNull
      private List<T> getCollectedValues(@NotNull Player player) {
        Document document = getDocument(player);
        List<T> values = new ArrayList<>(document.keys().size());
        for (String storedKey : document.keys()) {
          T value = parseValue(storedKey);
          if (value != null) values.add(value);
        }
        return values;
      }

      @Nullable
      private T parseValue(@NotNull String storedKey) {
        try {
          return Enum.valueOf(valueType, storedKey);
        } catch (IllegalArgumentException _) {
          return null;
        }
      }

      /**
       * Asks which of a handful of values the player collected the most of. Rejected when the player
       * has fewer than two values, or when all shown options are tied and the question would be
       * unanswerable in any meaningful way.
       */
      @Nullable
      private Question createMostQuestion(@NotNull Player player, @NotNull IRandom random) {
        List<T> collected = getCollectedValues(player);
        if (collected.size() < 2) return null;

        random.shuffle(collected);
        List<T> shown = collected.subList(0, Math.min(OPTION_COUNT, collected.size()));

        List<T> highest = new ArrayList<>(shown.size());
        double highestValue = Double.NEGATIVE_INFINITY;
        for (T value : shown) {
          double amount = getStatistic(player, value);
          if (amount > highestValue) {
            highestValue = amount;
            highest.clear();
            highest.add(value);
          } else if (amount == highestValue) {
            highest.add(value);
          }
        }
        if (highest.size() == shown.size()) return null; // everything is tied, there is nothing to ask

        List<Answer> options = shown.stream().map(Answer::of).toList();
        List<Answer> correctAnswers = highest.stream().map(Answer::of).toList();
        return new Question(messageKey("question.most").withArgs(getVerbMessage(), options.size()), options, correctAnswers);
      }

      /**
       * Asks for the exact amount collected for one random value.
       */
      @Nullable
      private Question createCountQuestion(@NotNull Player player, @NotNull IRandom random) {
        List<T> collected = getCollectedValues(player);
        if (collected.isEmpty()) return null;

        T value = random.choose(collected);
        int rightAnswer = (int) getStatistic(player, value);
        if (rightAnswer <= 0) return null;

        List<Answer> options = createNumberOptions(rightAnswer, random);
        return new Question(getCountQuestionMessage().withArgs(getVerbMessage(), value), options, List.of(Answer.of(rightAnswer)));
      }

    }

    /**
     * Builds the options for a numeric question: the right answer plus distinct, non-negative decoys
     * scattered around it. Decoys are drawn randomly and, should the draws keep colliding, filled up
     * deterministically, so this always terminates.
     */
    @NotNull
    static List<Answer> createNumberOptions(int rightAnswer, @NotNull IRandom random) {
      // the spread scales with the magnitude of the answer, otherwise large statistics would get
      // decoys that are trivially far off
      int spread = Math.max(rightAnswer / 10, MIN_DECOY_SPREAD);

      Set<Integer> values = new LinkedHashSet<>(OPTION_COUNT);
      values.add(rightAnswer);

      for (int draw = 0; values.size() < OPTION_COUNT && draw < MAX_DECOY_DRAWS; draw++) {
        int offset = random.nextInt(spread * 2 + 1) - spread;
        if (offset == 0) continue;
        int decoy = rightAnswer + offset;
        values.add(decoy < 0 ? rightAnswer - offset : decoy); // mirror instead of dropping the draw
      }
      for (int decoy = rightAnswer + 1; values.size() < OPTION_COUNT; decoy++) {
        values.add(decoy);
      }

      List<Answer> options = new ArrayList<>(values.size());
      for (int value : values) {
        options.add(Answer.of(value));
      }
      random.shuffle(options);
      return options;
    }

  }

  // Statistic collection

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onJump(@NotNull PlayerJumpEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    SavedStatistic.JUMPED.increaseStatistic(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onSneak(@NotNull PlayerToggleSneakEvent event) {
    if (!event.isSneaking()) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    SavedStatistic.SNEAKED.increaseStatistic(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getEntity() instanceof LivingEntity entity)) return;

    // also counts indirect damage, e.g. arrows and tnt the player is responsible for
    Player player = ChallengeHelper.getDamagerPlayer(event.getDamager());
    if (player == null || ignorePlayer(player)) return;

    double damage = ChallengeHelper.getFinalDamage(event);
    if (damage <= 0) return;

    SavedStatistic.MOB_DAMAGED.increaseStatistic(player, entity.getType(), damage);
    if (entity.getHealth() - damage > 0) return;

    SavedStatistic.MOB_KILLED.increaseStatistic(player, entity.getType());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull BlockPlaceEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    SavedStatistic.BLOCKS_PLACED.increaseStatistic(event.getPlayer(), event.getBlockPlaced().getType());
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onBlockBreak(@NotNull BlockBreakEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getBlock().getType().isAir()) return;
    SavedStatistic.BLOCKS_DESTROYED.increaseStatistic(event.getPlayer(), event.getBlock().getType());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;
    if (BlockUtils.isSameBlockIgnoreHeight(event.getFrom(), event.getTo())) return;
    SavedStatistic.BLOCKS_MOVED.increaseStatistic(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onItemDrop(@NotNull PlayerDropItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    SavedStatistic.ITEMS_DROPPED.increaseStatistic(event.getPlayer(), event.getItemDrop().getItemStack().getType());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    if (event.getCause() == DamageCause.CUSTOM) return; // don't count plugin damage

    double damage = ChallengeHelper.getFinalDamage(event);
    if (damage <= 0) return;

    SavedStatistic.DAMAGE_TAKEN.increaseStatistic(player, event.getCause(), damage);
  }

}
