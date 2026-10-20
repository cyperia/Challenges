package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.collection.SeededRandomWrapper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkRandomEffectChallenge extends Setting {

  /*
   * 	Saving potion effect to prevent the possibility that an effect remove was somehow skipped
   */
  private Map<UUID, PotionEffect> currentPotionEffects = new HashMap<>();

  private long worldSeed;

  @Override
  protected void onEnable() {
    currentPotionEffects = new HashMap<>();
    worldSeed = ChallengeAPI.getGameWorld(Environment.NORMAL).getSeed();
    broadcastFiltered(player -> {
      addEffect(player, player.getLocation());
    });
  }

  @Override
  protected void onDisable() {
    broadcastFiltered(player -> {
      removeEffect(player, player.getLocation());
    });
    worldSeed = 0;
    currentPotionEffects = null;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onGameModeChange(PlayerIgnoreStatusChangeEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.isIgnored()) {
      removeEffect(event.getPlayer(), event.getPlayer().getLocation());
    } else {
      addEffect(event.getPlayer(), event.getPlayer().getLocation());
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo().getChunk() == event.getFrom().getChunk()) return;
    removeEffect(event.getPlayer(), event.getFrom());
    addEffect(event.getPlayer(), event.getTo());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    if (event.getTo() == null) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo().getChunk() == event.getFrom().getChunk()) return;
    removeEffect(event.getPlayer(), event.getFrom());
    addEffect(event.getPlayer(), event.getTo());
  }

  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    broadcastFiltered(player -> addEffect(player, player.getLocation()));
  }

  @TimerTask(status = TimerStatus.PAUSED, async = false)
  public void onTimerPause() {
    broadcastFiltered(player -> removeEffect(player, player.getLocation()));
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onTimerStart() {
    broadcastFiltered(player -> addEffect(player, player.getLocation()));
  }

  public void removeEffect(Player player, Location location) {
    PotionEffectType type = getEffect(location.getChunk()).getType();
    player.removePotionEffect(type);

    removeEffectInCache(player);
  }

  public void addEffect(Player player, Location location) {
    Chunk chunk = location.getChunk();
    PotionEffect effect = getEffect(chunk);

    removeEffectInCache(player);

    if (player.hasPotionEffect(effect.getType())) return;
    player.addPotionEffect(effect);
    currentPotionEffects.put(player.getUniqueId(), effect);
  }

  public void removeEffectInCache(Player player) {
    PotionEffect currentEffect = currentPotionEffects.remove(player.getUniqueId());
    if (currentEffect != null) {
      player.removePotionEffect(currentEffect.getType());
    }
  }

  public PotionEffect getEffect(Chunk chunk) {

    IRandom random = new SeededRandomWrapper(worldSeed * (
      (long) (chunk.getX() == 0 ? Integer.MAX_VALUE : chunk.getX()) * (chunk.getZ() == 0 ? Integer.MAX_VALUE : chunk.getZ())));

    PotionEffectType[] types = PotionEffectType.values();
    PotionEffectType type = types[random.nextInt(types.length)];

    return type.createEffect(PotionEffect.INFINITE_DURATION, random.nextInt(type.isInstant() ? 1 : 4));
  }

}
