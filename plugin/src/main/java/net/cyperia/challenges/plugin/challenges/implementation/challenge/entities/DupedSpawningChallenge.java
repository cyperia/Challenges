package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;

public class DupedSpawningChallenge extends Setting {

  private boolean inCustomSpawn = false;

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawn(@NotNull EntitySpawnEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getEntity() instanceof LivingEntity)
      || event.getEntity() instanceof Player
      || event.getEntity() instanceof EnderDragon
    ) return;
    if (inCustomSpawn) return;
    inCustomSpawn = true;
    Entity entity = event.getEntity().getWorld().spawnEntity(event.getLocation(), event.getEntityType());

    if (entity instanceof Slime && event.getEntity() instanceof Slime) {
      ((Slime) entity).setSize(((Slime) event.getEntity()).getSize());
    }

    inCustomSpawn = false;
  }

}
