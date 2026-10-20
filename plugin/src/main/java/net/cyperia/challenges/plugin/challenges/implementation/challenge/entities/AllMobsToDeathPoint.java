package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.EntityDeathByPlayerEvent;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AllMobsToDeathPoint extends Setting {

  @EventHandler
  public void onEntityDeath(@NotNull EntityDeathByPlayerEvent event) {
    if (event.getEntity() instanceof EnderDragon || event.getEntity() instanceof Player) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;
    if (!shouldExecuteEffect(event.getKiller())) return;

    teleportAllMobsOfType(event.getEntityType(), event.getEntity().getLocation());
  }

  private void teleportAllMobsOfType(@NotNull EntityType entityType, @NotNull Location location) {
    if (location.getWorld() == null) return;
    Collection<Entity> entities = location.getWorld().getEntitiesByClasses(entityType.getEntityClass());
    for (Entity entity : entities) {
      if (!(entity instanceof LivingEntity)) return;
      ((LivingEntity) entity).setNoDamageTicks(20);
      entity.teleport(location);
    }
  }

}
