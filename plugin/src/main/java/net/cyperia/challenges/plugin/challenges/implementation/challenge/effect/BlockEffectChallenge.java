package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.collection.SeededRandomWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockEffectChallenge extends Setting {

  /*
   * 	Saving potion effect to prevent the possibility that an effect remove was somehow skipped
   */
  private Map<UUID, PotionEffect> currentPotionEffects = new HashMap<>();

  @Override
  protected void onEnable() {
    currentPotionEffects = new HashMap<>();
    if (!shouldExecuteEffect()) return;
    broadcastFiltered(player -> {
      addEffect(player, player.getLocation(), null);
    });
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onStart() {
    broadcastFiltered(player -> {
      addEffect(player, player.getLocation(), null);
    });
  }

  @Override
  protected void onDisable() {
    broadcastFiltered(player -> {
      removeEffect(player, player.getLocation(), null);
    });
    currentPotionEffects = null;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onLeave(PlayerQuitEvent event) {
    if (!shouldExecuteEffect()) return;
    currentPotionEffects.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onGameModeChange(PlayerIgnoreStatusChangeEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.isIgnored()) {
      removeEffect(event.getPlayer(), event.getPlayer().getLocation(), null);
    } else {
      addEffect(event.getPlayer(), event.getPlayer().getLocation(), null);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;
    Block fromBlock = BlockUtils.getBlockBelow(event.getFrom());
    Block toBlock = BlockUtils.getBlockBelow(event.getTo());
    if (toBlock != null && fromBlock != null) {
      if (toBlock.getType() == fromBlock.getType()) return;
    }
    removeEffect(event.getPlayer(), event.getFrom(), fromBlock);
    addEffect(event.getPlayer(), event.getTo(), toBlock);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;
    Block fromBlock = BlockUtils.getBlockBelow(event.getFrom());
    Block toBlock = BlockUtils.getBlockBelow(event.getTo());
    if (toBlock != null && fromBlock != null) {
      if (toBlock.getType() == fromBlock.getType()) return;
    }
    removeEffect(event.getPlayer(), event.getFrom(), fromBlock);
    addEffect(event.getPlayer(), event.getTo(), toBlock);
  }

  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    broadcastFiltered(player -> {
      addEffect(player, player.getLocation(), null);
    });
  }

  public void removeEffect(Player player, Location location, Block blockBelow) {
    if (blockBelow == null) {
      blockBelow = BlockUtils.getBlockBelow(location, -1);
    }
    if (blockBelow == null) return;
    PotionEffectType type = getEffect(blockBelow.getType()).getType();
    player.removePotionEffect(type);

    removeEffectInCache(player);
  }


  public void addEffect(Player player, Location location, Block blockBelow) {
    if (blockBelow == null) {
      blockBelow = BlockUtils.getBlockBelow(location, -1);
    }
    if (blockBelow == null) return;
    PotionEffect effect = getEffect(blockBelow.getType());
    PotionEffect currentEffect = currentPotionEffects.get(player.getUniqueId());
    if (currentEffect != null && !currentEffect.equals(effect)) {
      player.removePotionEffect(currentEffect.getType());
    } else if (player.hasPotionEffect(effect.getType())) return;
    player.addPotionEffect(effect);
    currentPotionEffects.put(player.getUniqueId(), effect);
  }

  public void removeEffectInCache(Player player) {
    PotionEffect currentEffect = currentPotionEffects.get(player.getUniqueId());
    if (currentEffect != null) {
      player.removePotionEffect(currentEffect.getType());
    }
  }

  private PotionEffect getEffect(Material material) {
    long seed = ChallengeAPI.getGameWorld(World.Environment.NORMAL).getSeed();
    IRandom random = new SeededRandomWrapper(seed / material.ordinal());
    PotionEffectType[] types = PotionEffectType.values();
    PotionEffectType type = types[random.nextInt(types.length)];
    return type.createEffect(PotionEffect.INFINITE_DURATION, random.nextInt(type.isInstant() ? 1 : 4));
  }

}
