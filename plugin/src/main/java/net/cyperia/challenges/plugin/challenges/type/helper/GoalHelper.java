package net.cyperia.challenges.plugin.challenges.type.helper;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.management.challenges.ChallengeManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntBiFunction;

public final class GoalHelper {

  private GoalHelper() {
  }

  public static void handleSetEnabled(@NotNull IGoal goal, boolean enabled) {
    ChallengeManager manager = Challenges.getInstance().getChallengeManager();
    if (manager.getCurrentGoal() != goal && enabled) {
      manager.setCurrentGoal(goal);
      goal.playStatusUpdateTitle();
    } else if (manager.getCurrentGoal() == goal && !enabled) {
      manager.setCurrentGoal(null);
      goal.playStatusUpdateTitle();
    }
  }

  @NotNull
  public static <V> Map<Player, Integer> createPointsFromValues(@NotNull AtomicInteger mostPoints, @NotNull Map<UUID, V> map, @NotNull ToIntBiFunction<UUID, V> mapper) {
    Map<Player, Integer> result = new HashMap<>();
    for (Entry<UUID, V> entry : map.entrySet()) {
      Player player = Bukkit.getPlayer(entry.getKey());
      if (player == null || !countsPoints(player))
        continue;
      int points = mapper.applyAsInt(entry.getKey(), entry.getValue());
      if (points == 0)
        continue;

      result.put(player, points);

      if (points >= mostPoints.get())
        mostPoints.set(points);
    }
    return result;
  }

  /**
   * Every playing player, with 0 points too, and every player who left with at least one point.
   */
  @NotNull
  public static <V> Map<OfflinePlayer, Integer> createLeaderboardFromValues(@NotNull Map<UUID, V> map, @NotNull ToIntBiFunction<UUID, V> mapper) {
    Map<OfflinePlayer, Integer> result = new HashMap<>();
    for (Player player : ChallengeAPI.getIngamePlayers()) {
      result.put(player, 0);
    }
    for (Entry<UUID, V> entry : map.entrySet()) {
      int points = mapper.applyAsInt(entry.getKey(), entry.getValue());
      Player player = Bukkit.getPlayer(entry.getKey());
      if (player == null) {
        if (points >= 1) result.put(Bukkit.getOfflinePlayer(entry.getKey()), points);
      } else if (points != 0 && countsPoints(player)) {
        result.put(player, points);
      }
    }
    return result;
  }

  // Spectators do not count while playing, but are shown again after the challenge ended
  private static boolean countsPoints(@NotNull Player player) {
    return !AbstractChallenge.ignorePlayer(player) || (ChallengeAPI.isPaused() && player.getGameMode() == GameMode.SPECTATOR);
  }

  public static void getWinnersOnEnd(@NotNull List<Player> winners, @NotNull Map<Player, Integer> points) {
    int mostPoints = 0;
    List<Player> currentWinners = new LinkedList<>();

    for (Entry<Player, Integer> entry : points.entrySet()) {
      int value = entry.getValue();
      if (value <= 0) continue;
      if (value == mostPoints) {
        currentWinners.add(entry.getKey());
      } else if (value > mostPoints) {
        mostPoints = value;
        currentWinners.clear();
        currentWinners.add(entry.getKey());
      }
    }

    winners.addAll(currentWinners);
  }

}
