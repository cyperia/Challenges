package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.ForceTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.challenges.plugin.content.i18n.*;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class ForceBattleGoal<T extends ForceTarget<?>> extends MenuGoal {

  protected final Map<UUID, Integer> jokerUsed = new HashMap<>();
  protected final Map<UUID, List<T>> foundTargets = new HashMap<>();
  protected final Map<UUID, T> currentTarget = new HashMap<>();
  protected T[] targetsPossibleToFind;
  private ItemStack jokerItem;

  protected final NumberSubSetting jokerCountSetting;
  protected final BooleanSubSetting scoreboardSetting;
  protected final BooleanSubSetting dupedTargetsSetting;

  public ForceBattleGoal() {

    jokerCountSetting = registerSetting("jokers", new NumberSubSetting(
      new ItemStack(Material.BARRIER), MessageKeySpace.of("challenge.force-battles.sub.goal-jokers"),
      0, 50, 5
    ));
    scoreboardSetting = registerSetting("showScoreboard", new BooleanSubSetting(
      new ItemStack(Material.BOOK), MessageKeySpace.of("challenge.force-battles.sub.scoreboard"),
      true
    ));
    if (shouldRegisterDupedTargetsSetting()) {
      dupedTargetsSetting = registerSetting("dupedTargets", new BooleanSubSetting(
        new ItemStack(Material.PAPER), MessageKeySpace.of("challenge.force-battles.sub.duped-targets"),
        true
      ));
    } else {
      dupedTargetsSetting = null;
    }
  }

  @Override
  protected void onEnable() {
    // one shared item for everyone, the identity checks below compare against it, so it cannot be
    // built per player language
    jokerItem = new ItemBuilder(Material.BARRIER)
      .hideAttributes()
      .setLines(MessageKey.of("challenge.force-battles.joker").asComponents(TranslationManager.getServerLocale()))
      .build();

    targetsPossibleToFind = getTargetsPossibleToFind();

    broadcastFiltered(this::updateJokersInInventory);
    if (ChallengeAPI.isStarted()) {
      broadcastFiltered(this::setRandomTargetIfCurrentlyNone);
    }

    setScoreboardContent();
    if (showScoreboard()) {
      scoreboard.show();
    }
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    if (jokerItem == null) return; // Disable through plugin disable
    broadcastFiltered(this::updateJokersInInventory);
    jokerItem = null;
    scoreboard.hide();
    bossbar.hide();
    targetsPossibleToFind = null;
  }

  protected abstract T[] getTargetsPossibleToFind();

  private void updateJokersInInventory(Player player) {
    PlayerInventory inventory = player.getInventory();
    boolean enabled = isEnabled() && ChallengeAPI.isStarted();
    int usableJokers = getUsableJokers(player.getUniqueId());

    int jokersInInventory = 0;

    LinkedList<ItemStack> itemStacks = new LinkedList<>(Arrays.asList(inventory.getContents()));
    itemStacks.add(player.getItemOnCursor());
    for (ItemStack itemStack : itemStacks) {
      if (jokerItem.isSimilar(itemStack)) {
        if (enabled) {
          jokersInInventory += itemStack.getAmount();
          if (jokersInInventory >= usableJokers) {
            int jokersToSubtract = jokersInInventory - usableJokers;
            jokersInInventory -= jokersToSubtract;
            itemStack.setAmount(itemStack.getAmount() - jokersToSubtract);
          }
        } else {
          inventory.removeItem(itemStack);
        }
      }

    }

    if (enabled) {
      if (jokersInInventory < usableJokers) {
        ItemStack clone = jokerItem.clone();
        clone.setAmount(usableJokers - jokersInInventory);
        InventoryUtils.dropOrGiveItem(inventory, player.getLocation(), clone);
      }
    }

  }

  @Override
  public void loadGameState(@NotNull Document document) {
    this.jokerUsed.clear();
    this.currentTarget.clear();
    this.foundTargets.clear();

    List<Document> players = document.getDocumentList("players");
    for (Document player : players) {
      UUID uuid = player.getUUID("uuid");

      T currentTarget = getTargetFromDocument(player, "currentTarget");

      if (currentTarget != null) {
        this.currentTarget.put(uuid, currentTarget);
      }
      List<T> foundItems = getListFromDocument(player, "foundTargets");
      this.foundTargets.put(uuid, foundItems);

      int jokerUsed = player.getInt("jokerUsed");
      this.jokerUsed.put(uuid, jokerUsed);
    }

    if (isEnabled()) {
      if (ChallengeAPI.isStarted()) {
        broadcastFiltered(this::setRandomTargetIfCurrentlyNone);
      }
      updateScoreboard();
      broadcastFiltered(this::updateJokersInInventory);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    List<Document> playersDocuments = new LinkedList<>();
    for (Map.Entry<UUID, T> entry : currentTarget.entrySet()) {
      List<T> foundItems = this.foundTargets.get(entry.getKey());
      int jokerUsed = this.jokerUsed.getOrDefault(entry.getKey(), 0);
      GsonDocument playerDocument = new GsonDocument();
      playerDocument.set("uuid", entry.getKey());
      setTargetInDocument(playerDocument, "currentTarget", entry.getValue());
      if (foundItems != null) {
        setFoundListInDocument(playerDocument, "foundTargets", foundItems);
      }
      playerDocument.set("jokerUsed", jokerUsed);
      playersDocuments.add(playerDocument);
    }

    document.set("players", playersDocuments);
  }

  public void setTargetInDocument(Document document, String path, T target) {
    document.set(path, target.getTargetSaveObject());
  }

  public void setFoundListInDocument(Document document, String path, List<T> targets) {
    document.set(path, targets.stream().map(ForceTarget::getTargetSaveObject).collect(Collectors.toList()));
  }

  public abstract T getTargetFromDocument(Document document, String path);

  public abstract List<T> getListFromDocument(Document document, String path);

  public void setRandomTargetIfCurrentlyNone(Player player) {
    if (currentTarget.containsKey(player.getUniqueId())) {
      return;
    }
    setRandomTarget(player);
  }

  public void setRandomTarget(Player player) {
    T target = getRandomTarget(player);

    if (target != null) {
      currentTarget.put(player.getUniqueId(), target);
      getNewTargetMessage(target).send(player, Prefix.CHALLENGES, target.getDisplayName());
      SoundSample.PLING.play(player);
    } else {
      currentTarget.remove(player.getUniqueId());
      SoundSample.BASS_OFF.play(player);
    }
    updateScoreboard();

  }

  /**
   * The sidebar is opt-out via {@link #scoreboardSetting}, so a target change while it is turned off
   * must not run into {@code ChallengeScoreboard}'s "not shown" warning.
   */
  protected void updateScoreboard() {
    bossbar.update();
    if (!scoreboard.isShown()) return;
    scoreboard.update();
  }

  protected T getRandomTarget(Player player) {
    LinkedList<T> list = new LinkedList<>(Arrays.asList(targetsPossibleToFind));
    if (shouldRegisterDupedTargetsSetting() && !dupedTargetsSetting.getAsBoolean()) {
      list.removeAll(foundTargets.getOrDefault(player.getUniqueId(), new LinkedList<>()));
    }
    if (!list.isEmpty()) {
      return globalRandom.choose(list);
    }
    return null;
  }

  protected MessageKey getNewTargetMessage(T newTarget) {
    return newTarget.getNewTargetMessage();
  }

  protected MessageKey getTargetCompletedMessage(T target) {
    return target.getCompletedMessage();
  }

  public void handleTargetFound(Player player) {
    T foundTarget = currentTarget.get(player.getUniqueId());
    if (foundTarget != null) {
      List<T> list = foundTargets.computeIfAbsent(player.getUniqueId(), _ -> new LinkedList<>());
      list.add(foundTarget);
      getTargetCompletedMessage(foundTarget).send(player, Prefix.CHALLENGES, foundTarget.getDisplayName());
    }
    setRandomTarget(player);
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    Bukkit.getScheduler().runTask(plugin, () -> sendLeaderboard(null));
  }

  public void sendResult(@NotNull Player player) {
    Bukkit.getScheduler().runTask(plugin, () -> sendLeaderboard(player));
  }

  /**
   * @param receiver receiver of the leaderboard, or {@code null} to broadcast it to everyone
   */
  private void sendLeaderboard(@Nullable Player receiver) {
    GoalLeaderboard.sendChatLeaderboard(getLeaderboardTitleMessage(),
      GoalLeaderboard.rank(foundTargets, List::size), receiver);
  }

  @NotNull
  protected MessageKey getLeaderboardTitleMessage() {
    return getChallengeMessageKey("leaderboard");
  }

  /**
   * One point per target found, so that the sidebar ranks the same way the result leaderboard does.
   */
  @NotNull
  protected Map<Player, Integer> getPoints(@NotNull AtomicInteger mostPoints, boolean zeros) {
    return GoalHelper.createPointsFromValues(mostPoints, foundTargets, (_, targets) -> targets.size(), zeros);
  }

  private int getUsableJokers(UUID uuid) {
    return Math.max(0, getJokers() - jokerUsed.getOrDefault(uuid, 0));
  }

  public void handleJokerUse(Player player) {
    if (currentTarget.get(player.getUniqueId()) == null) {
      setRandomTargetIfCurrentlyNone(player);
      return;
    }
    if (getUsableJokers(player.getUniqueId()) <= 0) {
      MessageKey.of("challenge.force-battles.no-jokers-left").send(player, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.play(player);
      updateJokersInInventory(player);
      return;
    }

    int jokerUsed = this.jokerUsed.getOrDefault(player.getUniqueId(), 0);
    jokerUsed++;
    this.jokerUsed.put(player.getUniqueId(), jokerUsed);
    handleTargetFound(player);
    updateJokersInInventory(player);
  }

  protected void setScoreboardContent() {
    scoreboard.setContent(GoalLeaderboard.scoreboardContent(() -> getPoints(new AtomicInteger(), true)));
    bossbar.setContent((bar, player) -> {
      bar.setColor(BossBar.Color.WHITE);
      LocalizableMessage target = getCurrentTargetDisplay(player);
      int targetsFound = getTargetFoundCount(player);
      bar.setTitle(MessageKey.of("challenge.force-battles.bossbar"), target, targetsFound);
    });
  }

  @NotNull
  private LocalizableMessage getCurrentTargetDisplay(@NotNull Player player) {
    T target = currentTarget.get(player.getUniqueId());
    return target == null ? MessageKey.of("generic.none") : getScoreboardTargetDisplay(target);
  }

  /**
   * @implNote every player hunts the same kind of target here, so the bare name says enough; goals
   * handing out mixed target types override this with {@link ForceTarget#getTypedDisplayName()}
   */
  @NotNull
  protected LocalizableMessage getScoreboardTargetDisplay(@NotNull T target) {
    return target.getDisplayName();
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onStart() {
    broadcastFiltered(this::setRandomTargetIfCurrentlyNone);
    broadcastFiltered(this::updateJokersInInventory);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(PlayerJoinEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    setRandomTargetIfCurrentlyNone(event.getPlayer());
    updateJokersInInventory(event.getPlayer());
  }

  @ScheduledTask(ticks = 1, async = false, timerPolicy = TimerPolicy.ALWAYS)
  public void onTick() {
    if (!isEnabled()) return;

    if (!scoreboard.isShown() && showScoreboard()) {
      scoreboard.show();
    } else if (scoreboard.isShown() && !showScoreboard()) {
      scoreboard.hide();
    }
    broadcastFiltered(this::updateJokersInInventory);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteract(PlayerInteractEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getAction() != Action.RIGHT_CLICK_AIR
      && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (jokerItem.isSimilar(event.getItem())) {
      handleJokerUse(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDropItem(PlayerDropItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (jokerItem.isSimilar(event.getItemDrop().getItemStack())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDeath(PlayerDeathEvent event) {
    if (!shouldExecuteEffect(event.getEntity())) return;
    event.getDrops().removeIf(itemStack -> jokerItem.isSimilar(itemStack));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onRespawn(PlayerRespawnEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    Bukkit.getScheduler().runTask(plugin, () -> {
      updateJokersInInventory(event.getPlayer());
    });
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (jokerItem.isSimilar(event.getItemInHand())) {
      event.setCancelled(true);
    }
  }

  protected int getTargetFoundCount(Player player) {
    List<T> found = foundTargets.get(player.getUniqueId());
    if (found == null) return 0;
    return found.size();
  }

  private int getJokers() {
    return jokerCountSetting.getAsInt();
  }

  private boolean showScoreboard() {
    return scoreboardSetting.getAsBoolean();
  }

  protected boolean shouldRegisterDupedTargetsSetting() {
    return true;
  }

}
