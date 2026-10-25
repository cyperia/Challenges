package net.cyperia.challenges.plugin.management.server.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import lombok.Getter;
import lombok.ToString;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.hook.LanguageProvider;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentArguments;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class ChallengeScoreboard {

  public static final int MAX_LINES = 15;

  private final Map<Player, Objective> objectives = new ConcurrentHashMap<>();
  private BiConsumer<ScoreboardInstance, Player> content = (_, _) -> {
  };

  public void setContent(@NotNull BiConsumer<ScoreboardInstance, Player> content) {
    this.content = content;
  }

  public void applyHide(@NotNull Player player) {
    Objective objective = objectives.remove(player);
    if (objective == null) return;
    Regions.global(Challenges.getInstance(), () -> unregister(objective));
  }

  public void update() {
    for (Player player : new LinkedList<>(Challenges.getInstance().getPlayerManager().getAudience())) {
      update(player);
    }
  }

  // Runs on the global region, so two updates of one player never overlap: the objective name is
  // constant and sending it twice at once crashes the client ("An objective with the name 'xxx' already exists!")
  public void update(@NotNull Player player) {
    Regions.global(Challenges.getInstance(), () -> updateOnGlobal(player));
  }

  private void updateOnGlobal(@NotNull Player player) {
    if (!isShown()) {
      Logger.warn("Tried to update scoreboard which is not shown");
      return;
    }

    try {
      if (objectives.containsKey(player)) {
        unregister(objectives.remove(player));
      }

      ScoreboardInstance instance = new ScoreboardInstance();
      content.accept(instance, player);

      LanguageProvider languageProvider = Challenges.getInstance().getTranslationManager().getLanguageProvider();
      Locale locale = languageProvider.getPlayerLanguage(player);

      ArrayList<Component> lines = instance.getLines(locale);
      if (lines.isEmpty()) {
        return;
      }
      Component title = instance.getTitle().asComponent(locale);
      // Empty unless a server sets it, e.g. to its address
      Component footer = MessageKey.of("scoreboard.footer").asComponent(locale);
      if (!isBlank(footer) && lines.size() + 2 <= MAX_LINES) {
        if (!isBlank(lines.getLast())) lines.add(Component.empty());
        lines.add(centered(footer, title, lines));
      }

      PlayerScoreboards.use(player, scoreboard -> show(scoreboard, player, title, lines));

    } catch (Exception ex) {
      Logger.error("Unable to update scoreboard for player '{}'", player.getName(), ex);
    }
  }

  private void show(@NotNull Scoreboard scoreboard, @NotNull Player player, @NotNull Component title, @NotNull List<Component> lines) {
    String name = String.valueOf(player.getUniqueId().hashCode());
    // Unregister any old objective existing
    Objective oldObjective = scoreboard.getObjective(name);
    if (oldObjective != null) {
      unregister(oldObjective);
    }

    Objective objective = registerDummyObjective(scoreboard, name, title);
    for (int i = 0; i < lines.size(); i++) {
      Component line = lines.get(i);
      Score entry = objective.getScore(String.valueOf(i));
      entry.customName(line);
      entry.setScore(MAX_LINES - i);
      entry.numberFormat(NumberFormat.blank());
    }

    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    objectives.put(player, objective);
  }

  private static boolean isBlank(@NotNull Component line) {
    return PlainTextComponentSerializer.plainText().serialize(line).isBlank();
  }

  // Pads the line with spaces to the middle of the widest line, the sidebar's width
  @NotNull
  static Component centered(@NotNull Component line, @NotNull Component title, @NotNull List<Component> lines) {
    int widest = MinecraftFont.widthOf(title);
    for (Component other : lines) {
      widest = Math.max(widest, MinecraftFont.widthOf(other));
    }
    int spaces = (widest - MinecraftFont.widthOf(line)) / 2 / MinecraftFont.SPACE_WIDTH;
    return spaces <= 0 ? line : Component.text(" ".repeat(spaces)).append(line);
  }

  public void show() {
    Challenges.getInstance().getScoreboardManager().setCurrentScoreboard(this);
  }

  public void hide() {
    if (Challenges.getInstance().getScoreboardManager().getCurrentScoreboard() != this) return;
    Challenges.getInstance().getScoreboardManager().setCurrentScoreboard(null);
  }

  public boolean isShown() {
    return Challenges.getInstance().getScoreboardManager().isShown(this);
  }

  private void unregister(@Nullable Objective objective) {
    try {
      if (objective == null) return;
      objective.unregister();
    } catch (Exception ex) {
      Logger.error("Unable to unregister objective " + objective.getName());
    }
  }

  @NotNull
  private Objective registerDummyObjective(@NotNull Scoreboard scoreboard, @NotNull String name, @NotNull Component displayName) {
    return scoreboard.registerNewObjective(name, Criteria.DUMMY, displayName);
  }

  @ToString
  public static final class ScoreboardInstance {

    private final Object[] lines = new Object[MAX_LINES]; // either LocalizableMessage or Component
    @Getter
    private LocalizableMessage title = MessageKey.of("scoreboard.title");
    private int linesIndex = 0;

    private ScoreboardInstance() {
    }

    @NotNull
    public ScoreboardInstance addEmptyLine() {
      return addLine(Component.empty());
    }

    public ScoreboardInstance addLine(@NotNull Component text) {
      addLine((Object) text);
      return this;
    }

    @NotNull
    public ScoreboardInstance addLine(@NotNull LocalizableMessage line) {
      addLine((Object) line);
      return this;
    }

    @NotNull
    public ScoreboardInstance addLine(@NotNull MessageKey line, @NotNull Object... args) {
      if (args.length == 0) {
        addLine((Object) line);
      } else {
        addLine((Object) line.withArgs(args));
      }
      return this;
    }

    private void addLine(@NotNull Object lineObj) {
      if (linesIndex >= lines.length)
        throw new IllegalStateException("All lines are already used! (" + lines.length + ")");
      lines[linesIndex++] = lineObj;
    }

    @NotNull
    public ArrayList<Component> getLines(@NotNull Locale locale) {
      ArrayList<Component> list = new ArrayList<>(lines.length);
      for (Object line : lines) {
        if (line == null) continue;

        if (line instanceof LocalizableMessage message) {
          list.add(message.asComponent(locale));
        } else {
          list.add(ComponentArguments.convertToComponent(line));
        }
      }
      return list;
    }

    @NotNull
    public ScoreboardInstance setTitle(@NotNull LocalizableMessage title) {
      this.title = title;
      return this;
    }

    public int getRemainingLines() {
      return MAX_LINES - linesIndex;
    }
  }

}
