package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.EntityDamageByPlayerEvent;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RandomTeleportOnHitChallenge extends Setting {

  public static void switchEntityLocations(LivingEntity entity1, LivingEntity entity2) {
    Regions.entity(plugin, entity1, () -> entity1.setInvisible(true));
    Regions.entity(plugin, entity2, () -> entity2.setInvisible(false));
    Location entity2Location = entity2.getLocation().clone();
    entity2.teleportAsync(entity1.getLocation())
      .thenCompose(success -> entity1.teleportAsync(entity2Location))
      .thenRun(() -> {
        Regions.entity(plugin, entity2, () -> entity2.setInvisible(false));
        Regions.entity(plugin, entity1, () -> entity1.setInvisible(false));
      });
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityDamageByPlayer(EntityDamageByPlayerEvent event) {
    if (!shouldExecuteEffect(event.getDamager())) return;

    World world = event.getDamager().getWorld();
    List<LivingEntity> livingEntities = new CopyOnWriteArrayList<>();
    // The candidates of every region are gathered before one is chosen, the swap then runs on the global region
    Regions.forEachRegion(plugin, world, () -> livingEntities.addAll(world.getLivingEntities())).thenRun(() -> {
      livingEntities.removeIf(entity -> entity == event.getDamager() || entity instanceof Player && ignorePlayer((Player) entity));
      if (livingEntities.isEmpty()) return;
      switchEntityLocations(globalRandom.choose(livingEntities), event.getDamager());
    });
  }

}
