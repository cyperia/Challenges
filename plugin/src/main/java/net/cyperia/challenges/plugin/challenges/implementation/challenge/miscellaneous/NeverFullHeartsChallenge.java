package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;

public class NeverFullHeartsChallenge extends Setting {

  // One heart below full, where the original starts
  private static final double START_BELOW_FULL = 2;

  public NeverFullHeartsChallenge() {
    setUsesGlobalPunish(true);
  }

  static boolean isFull(double health, double maxHealth) {
    return health >= maxHealth;
  }

  @Override
  protected void onEnable() {
    if (ChallengeAPI.isStarted()) leaveFullHealth();
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onTimerStart() {
    if (!isEnabled()) return;
    leaveFullHealth();
  }

  // A full hunger bar keeps natural regeneration going; without saturation it heals at the slow rate
  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    if (!shouldExecuteEffect()) return;
    broadcastFiltered(player -> {
      player.setFoodLevel(20);
      player.setSaturation(0);
    });
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onRegain(@NotNull EntityRegainHealthEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    // The new health is only set once the event has passed
    Bukkit.getScheduler().runTask(plugin, () -> check(player));
  }

  private void check(@NotNull Player player) {
    if (!player.isOnline() || player.isDead() || !shouldExecuteEffect(player)) return;
    if (!isFull(player.getHealth(), player.getMaxHealth())) return;
    getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, player);
    ChallengeHelper.punish(player);
  }

  private void leaveFullHealth() {
    broadcastFiltered(player -> {
      player.setSaturation(0);
      if (isFull(player.getHealth(), player.getMaxHealth())) player.setHealth(player.getMaxHealth() - START_BELOW_FULL);
    });
  }

}
