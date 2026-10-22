package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NoSameHeartsChallenge extends Setting {

  public NoSameHeartsChallenge() {
    setUsesGlobalPunish(true);
  }

  // What the health bar shows: a heart that is not empty counts as half
  static int halfHearts(double health) {
    return (int) Math.ceil(health);
  }

  // Players start two hearts apart, since everyone joins with full health
  static double startHealth(double maxHealth, int index) {
    return Math.max(1, maxHealth - 4 * index);
  }

  @Override
  protected void onEnable() {
    if (ChallengeAPI.isStarted()) spreadHealth();
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onTimerStart() {
    if (!isEnabled()) return;
    spreadHealth();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    checkLater(event);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onRegain(@NotNull EntityRegainHealthEvent event) {
    checkLater(event);
  }

  private void checkLater(@NotNull EntityEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    // The new health is only set once the event has passed
    Bukkit.getScheduler().runTask(plugin, () -> check(player));
  }

  private void check(@NotNull Player player) {
    if (!player.isOnline() || player.isDead() || !shouldExecuteEffect(player)) return;
    int hearts = halfHearts(player.getHealth());
    for (Player other : alivePlayers()) {
      if (other == player || halfHearts(other.getHealth()) != hearts) continue;
      getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, player, other);
      ChallengeHelper.punish(player);
      return;
    }
  }

  private void spreadHealth() {
    List<Player> players = alivePlayers();
    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      // Without saturation the first heals come slowly, so nobody catches up within the first seconds
      player.setSaturation(0);
      player.setHealth(Math.min(player.getHealth(), startHealth(player.getMaxHealth(), i)));
    }
  }

  @NotNull
  private List<Player> alivePlayers() {
    List<Player> players = new ArrayList<>();
    broadcastFiltered(player -> {
      if (!player.isDead()) players.add(player);
    });
    return players;
  }

}
