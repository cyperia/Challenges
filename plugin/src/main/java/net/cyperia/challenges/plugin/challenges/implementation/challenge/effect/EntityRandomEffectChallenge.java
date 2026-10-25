package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class EntityRandomEffectChallenge extends Setting {

  private static final NamespacedKey AFFECTED = new NamespacedKey(Challenges.getInstance(), "entity_random_effect");

  @Override
  protected void onEnable() {
    for (World world : ChallengeAPI.getGameWorlds()) {
      Regions.forEachRegion(plugin, world, () -> world.getLivingEntities().forEach(this::addRandomEffect));
    }
  }

  @Override
  protected void onDisable() {
    for (World world : ChallengeAPI.getGameWorlds()) {
      Regions.forEachRegion(plugin, world, () -> world.getLivingEntities().forEach(EntityRandomEffectChallenge::removeEffects));
    }
  }

  // Mobs in chunks nobody was near when the challenge ended lose their effects as the chunk loads
  @EventHandler
  public void onEntitiesLoad(@NotNull EntitiesLoadEvent event) {
    if (isEnabled() || !ChallengeAPI.getGameWorlds().contains(event.getWorld())) return;
    for (Entity entity : event.getEntities()) {
      if (entity instanceof LivingEntity living && living.getPersistentDataContainer().has(AFFECTED)) {
        Regions.nextTick(plugin, living, () -> removeEffects(living));
      }
    }
  }

  private static void removeEffects(@NotNull LivingEntity entity) {
    if (entity.getType() == EntityType.PLAYER) return;
    for (PotionEffect effect : entity.getActivePotionEffects()) {
      entity.removePotionEffect(effect.getType());
    }
    entity.getPersistentDataContainer().remove(AFFECTED);
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
    entity.getPersistentDataContainer().set(AFFECTED, PersistentDataType.BOOLEAN, true);
  }

}
