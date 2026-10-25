package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.management.server.BorderManager;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LevelBorderChallenge extends Setting {

  private UUID bestPlayerUUID = null;
  private int bestPlayerLevel = 0;

  @Override
  protected void onEnable() {
    checkBorderSize(false);
  }

  @Override
  protected void onDisable() {
    border().clearGrowth(this);
  }

  public void checkBorderSize(boolean animate) {
    Player currentBestPlayer = bestPlayerUUID != null ? Bukkit.getPlayer(bestPlayerUUID) : null;

    for (Player player : ChallengeAPI.getIngamePlayers()) {
      int level = player.getLevel();

      if (bestPlayerUUID == null || currentBestPlayer == null) {
        bestPlayerLevel = level;
        bestPlayerUUID = player.getUniqueId();
        // Checks if the player is better than the saved level or if online the current player level.
        // Checking with the player instance is required to fix issues with dying and the level of the best player being 0.
      } else if (level > bestPlayerLevel || level > currentBestPlayer.getLevel()) {
        bestPlayerLevel = level;
        bestPlayerUUID = player.getUniqueId();
      } else if (player.getUniqueId().equals(bestPlayerUUID)) {
        bestPlayerLevel = level;
      }
    }

    border().setGrowth(this, bestPlayerLevel, animate);
  }

  @ScheduledTask(ticks = 100, async = false, timerPolicy = TimerPolicy.STARTED)
  public void checkBorderSize() {
    checkBorderSize(true);
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onTimerStart() {
    if (!isEnabled()) return;
    checkBorderSize(false);
    border().containPlayers();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onLevelChange(@NotNull PlayerLevelChangeEvent event) {
    if (!shouldExecuteEffect()) return;
    checkBorderSize(true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    if (!shouldExecuteEffect()) return;
    if (ignorePlayer(event.getPlayer())) return;
    Regions.later(plugin, () -> checkBorderSize(false), 1);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
    if (!shouldExecuteEffect()) return;
    if (ignorePlayer(event.getPlayer())) return;
    Regions.later(plugin, () -> checkBorderSize(false), 1);
  }

  /**
   * Execute level change event when dying instead of respawning like spigot does it
   */
  @EventHandler(priority = EventPriority.HIGH)
  public void onDeath(@NotNull PlayerDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    if (ignorePlayer(event.getEntity())) return;
    PlayerLevelChangeEvent lvlEvent = new PlayerLevelChangeEvent(
      event.getEntity(), event.getEntity().getLevel(), 0);
    event.getEntity().setLevel(0);
    onLevelChange(lvlEvent);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    bestPlayerLevel = document.getInt("level");
    String uuid = document.getString("uuid");
    if (uuid != null) {
      bestPlayerUUID = UUID.fromString(uuid);
    }

    if (isEnabled()) {
      checkBorderSize(false);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    document.set("level", bestPlayerLevel);
    document.set("uuid", bestPlayerUUID);
  }

  @NotNull
  private static BorderManager border() {
    return Challenges.getInstance().getBorderManager();
  }

}
