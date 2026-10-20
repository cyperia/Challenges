package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityRandomEffectChallenge extends Setting {

  @Override
  protected void onEnable() {
    for (World world : ChallengeAPI.getGameWorlds()) {
      for (LivingEntity entity : world.getLivingEntities()) {
        addRandomEffect(entity);
      }
    }
  }

  @Override
  protected void onDisable() {
    for (World world : ChallengeAPI.getGameWorlds()) {
      for (LivingEntity entity : world.getLivingEntities()) {
        if (entity.getType() == EntityType.PLAYER) continue;
        for (PotionEffect effect : entity.getActivePotionEffects()) {
          entity.removePotionEffect(effect.getType());
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onSpawn(EntitySpawnEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;
    addRandomEffect((LivingEntity) event.getEntity());
  }

  public void addRandomEffect(LivingEntity entity) {
    if (entity.getType() == EntityType.PLAYER) return;
    PotionEffectType[] types = PotionEffectType.values();
    PotionEffectType type = globalRandom.choose(types);
    entity.addPotionEffect(type.createEffect(PotionEffect.INFINITE_DURATION, 255));
  }

}
