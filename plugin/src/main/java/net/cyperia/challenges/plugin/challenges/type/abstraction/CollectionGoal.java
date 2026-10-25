package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CollectionGoal extends SettingGoal implements ChecklistGoal {

  private final Map<UUID, List<String>> collections = new ConcurrentHashMap<>();
  protected Object[] target;
  private Set<String> targetKeys;

  public CollectionGoal(@NotNull Object[] target) {
    applyTarget(target);
  }

  public CollectionGoal(boolean enabledByDefault, @NotNull Object[] target) {
    super(enabledByDefault);
    applyTarget(target);
  }

  @Override
  protected void onEnable() {
    scoreboard.setContent(GoalLeaderboard.scoreboardContent(this::getLeaderboard));
    scoreboard.show();
  }

  @Override
  protected void onDisable() {
    scoreboard.hide();
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    AtomicInteger mostPoints = new AtomicInteger();
    Map<Player, Integer> points = getPoints(mostPoints);
    if (mostPoints.get() == 0) return; // Nobody won, nobody has anything

    for (Map.Entry<Player, Integer> entry : points.entrySet()) {
      if (entry.getValue() != mostPoints.get()) continue;
      winners.add(entry.getKey());
    }
  }

  @NotNull
  protected Map<Player, Integer> getPoints(@NotNull AtomicInteger mostPoints) {
    return GoalHelper.createPointsFromValues(mostPoints, collections, (uuid, strings) -> getCollectedCount(uuid));
  }

  @NotNull
  protected Map<OfflinePlayer, Integer> getLeaderboard() {
    return GoalHelper.createLeaderboardFromValues(collections, (uuid, strings) -> getCollectedCount(uuid));
  }

  protected void collect(@NotNull Player player, @NotNull Object item, @NotNull Runnable success) {
    if (ignorePlayer(player)) return;
    List<String> collection = getCollectionRaw(player.getUniqueId());
    if (collection.contains(item.toString())) return;
    if (!targetKeys.contains(item.toString())) return;
    collection.add(item.toString());
    success.run();
    checkCollects();
  }

  protected int getCollectedCount(@NotNull UUID uuid) {
    int collected = 0;
    for (String entry : getCollectionRaw(uuid)) {
      if (targetKeys.contains(entry)) collected++;
    }
    return collected;
  }

  protected List<String> getCollectionRaw(@NotNull UUID uuid) {
    return collections.computeIfAbsent(uuid, key -> new CopyOnWriteArrayList<>());
  }

  protected void checkCollects() {
    scoreboard.update();
    for (Player player : ChallengeAPI.getIngamePlayers()) {
      if (getCollectedCount(player.getUniqueId()) >= target.length) {
        ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
        return;
      }
    }
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);

    collections.clear();
    Document scores = document.getDocument("scores");
    for (String key : scores.keys()) {
      try {
        UUID uuid = UUID.fromString(key);
        List<String> collection = scores.getStringList(key);
        collections.put(uuid, new CopyOnWriteArrayList<>(collection));
      } catch (Exception ex) {
        Logger.error("Could not load scores for {}", key);
      }
    }

    if (scoreboard.isShown()) {
      scoreboard.update();
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    Document scores = document.getDocument("scores");
    collections.forEach((uuid, collection) -> {
      scores.set(uuid.toString(), collection);
    });
  }

  @NotNull
  @Override
  public List<ChecklistEntry> getChecklist(@NotNull Player viewer) {
    List<ChecklistEntry> entries = new ArrayList<>(target.length);
    for (Object entry : target) {
      List<UUID> collectors = getCollectors(collections, entry.toString());
      entries.add(ChecklistEntry.of(entry, collectors.contains(viewer.getUniqueId()), collectors));
    }
    return entries;
  }

  @NotNull
  @Override
  public MessageKey getFinderMessage() {
    return MessageKey.of("menu.goal.collected-by");
  }

  @NotNull
  static List<UUID> getCollectors(@NotNull Map<UUID, List<String>> collections, @NotNull String key) {
    return collections.entrySet().stream()
      .filter(collection -> collection.getValue().contains(key))
      .map(Map.Entry::getKey)
      .toList();
  }

  protected void setTarget(@NotNull Object... target) {
    applyTarget(target);
  }

  // the target set is scanned on every scoreboard update, so the lookup keys are precomputed
  private void applyTarget(@NotNull Object[] target) {
    Set<String> keys = HashSet.newHashSet(target.length);
    for (Object entry : target) {
      keys.add(entry.toString());
    }
    this.target = target;
    this.targetKeys = keys;
  }

}
