package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.EntityDeathByPlayerEvent;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class AllMobsToDeathPoint extends Setting {

  @EventHandler
  public void onEntityDeath(@NotNull EntityDeathByPlayerEvent event) {
    if (event.getEntity() instanceof EnderDragon || event.getEntity() instanceof Player) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;
    if (!shouldExecuteEffect(event.getKiller())) return;

    teleportAllMobsOfType(event.getEntityType(), event.getEntity().getLocation());
  }

  private void teleportAllMobsOfType(@NotNull EntityType entityType, @NotNull Location location) {
    World world = location.getWorld();
    if (world == null) return;
    Regions.forEachRegion(plugin, world, () -> {
      for (Entity entity : world.getEntitiesByClasses(entityType.getEntityClass())) {
        if (!(entity instanceof LivingEntity living)) continue;
        living.setNoDamageTicks(20);
        living.teleportAsync(location);
      }
    });
  }

}
