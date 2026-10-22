package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class LocationEffectChallenge<R> extends Setting {

  // Only effects this challenge gave are taken away again, never a potion the player drank themselves
  private Map<UUID, PotionEffect> givenEffects = new HashMap<>();
  private long worldSeed;

  @NotNull
  protected abstract R regionAt(@NotNull Location location);

  @NotNull
  protected abstract PotionEffect effectFor(@NotNull R region);

  protected long worldSeed() {
    return worldSeed;
  }

  @Override
  protected void onEnable() {
    givenEffects = new HashMap<>();
    worldSeed = ChallengeAPI.getGameWorld(Environment.NORMAL).getSeed();
    broadcastFiltered(player -> update(player, player.getLocation()));
  }

  @Override
  protected void onDisable() {
    broadcastFiltered(this::clear);
    givenEffects = null;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onIgnoreStatusChange(PlayerIgnoreStatusChangeEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.isIgnored()) clear(event.getPlayer());
    else update(event.getPlayer(), event.getPlayer().getLocation());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    moved(event.getPlayer(), event.getFrom(), event.getTo());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    moved(event.getPlayer(), event.getFrom(), event.getTo());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onQuit(PlayerQuitEvent event) {
    // An infinite effect is saved with the player, so one left on would come back untracked after a rejoin
    if (givenEffects != null) clear(event.getPlayer());
  }

  // Puts back an effect the player lost another way, by milk or death
  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    broadcastFiltered(player -> update(player, player.getLocation()));
  }

  @TimerTask(status = TimerStatus.PAUSED, async = false)
  public void onTimerPause() {
    broadcastFiltered(this::clear);
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onTimerStart() {
    broadcastFiltered(player -> update(player, player.getLocation()));
  }

  private void moved(@NotNull Player player, @NotNull Location from, Location to) {
    if (to == null || !shouldExecuteEffect(player)) return;
    if (regionAt(from).equals(regionAt(to))) return;
    update(player, to);
  }

  private void update(@NotNull Player player, @NotNull Location location) {
    PotionEffect effect = effectFor(regionAt(location));
    PotionEffect given = givenEffects.get(player.getUniqueId());
    // Also when only the level differs, which a neighbouring region with the same effect type can roll
    if (given != null && !given.equals(effect)) {
      player.removePotionEffect(given.getType());
      givenEffects.remove(player.getUniqueId());
    }
    if (player.hasPotionEffect(effect.getType())) return;
    player.addPotionEffect(effect);
    givenEffects.put(player.getUniqueId(), effect);
  }

  private void clear(@NotNull Player player) {
    PotionEffect given = givenEffects.remove(player.getUniqueId());
    if (given != null) player.removePotionEffect(given.getType());
  }

}
