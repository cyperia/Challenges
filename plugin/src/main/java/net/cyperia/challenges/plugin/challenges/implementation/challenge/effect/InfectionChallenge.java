package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfectionChallenge extends Setting {

  @Override
  protected void onDisable() {
    broadcastAudience(this::removeEffects);
  }

  @ScheduledTask(ticks = 10, async = false)
  public void onHalfSecond() {
    if (!shouldExecuteEffect()) {
      broadcastAudience(this::removeEffects);
      return;
    }
    // An ignored player keeps whatever this challenge gave them until it is taken away again
    broadcastIgnored(this::removeEffects);
    broadcastFiltered(this::updateSickness);
  }

  private void updateSickness(@NotNull Player player) {
    List<Entity> entities = getNearbyTargets(player, 2);
    if (entities.isEmpty()) {
      player.removePotionEffect(PotionEffectType.NAUSEA);
      return;
    }

    byte value = 1;
    for (Entity currentEntity : entities) {
      if (player.getLocation().distance(currentEntity.getLocation()) <= 1.5) value = 2;
    }

    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, PotionEffect.INFINITE_DURATION, 15));
    if (value == 1) {
      if (!player.hasPotionEffect(PotionEffectType.WITHER)) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20, 2));
      }
    } else {
      player.removePotionEffect(PotionEffectType.POISON);
      player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 4));
    }

  }

  private void removeEffects(@NotNull Player player) {
    player.removePotionEffect(PotionEffectType.NAUSEA);
    player.removePotionEffect(PotionEffectType.POISON);
    player.removePotionEffect(PotionEffectType.WITHER);
  }

  private List<Entity> getNearbyTargets(@NotNull Player player, double range) {
    return player.getNearbyEntities(range, range, range).stream()
      .filter(entity -> entity != player)
      .filter(entity -> entity instanceof LivingEntity)
      .filter(entity -> !(entity instanceof Player target) || !ignorePlayer(target))
      .toList();
  }

}
