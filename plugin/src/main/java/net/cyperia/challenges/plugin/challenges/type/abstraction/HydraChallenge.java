package net.cyperia.challenges.plugin.challenges.type.abstraction;

import org.bukkit.Particle;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.spigot.events.EntityDeathByPlayerEvent;
import net.cyperia.challenges.plugin.utils.misc.ParticleUtils;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public abstract class HydraChallenge extends Setting {

  // A hydra chain feeds itself: every mob it spawns multiplies again when it dies, so without a
  // ceiling on the world's entity count it piles up mobs until the heap is exhausted.
  private static final int MAX_WORLD_ENTITIES = 5000;

  public HydraChallenge() {
  }

  public HydraChallenge(boolean enabledByDefault) {
    super(enabledByDefault);
  }

  public abstract int getNewMobsCount(@NotNull EntityType entityType);

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityDamageByEntity(@NotNull EntityDeathByPlayerEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getEntity() instanceof EnderDragon || event.getEntity() instanceof Player) return;
    if (ChallengeHelper.ignoreDamager(event.getKiller())) return;

    World world = event.getEntity().getWorld();
    int budget = MAX_WORLD_ENTITIES - world.getEntityCount();
    if (budget <= 0) return;

    // getNewMobsCount may raise a stored multiplier, so it must not run when nothing can spawn
    int mobsCount = Math.min(getNewMobsCount(event.getEntityType()), budget);

    for (int i = 0; i < mobsCount; i++) {
      world.spawnEntity(event.getEntity().getLocation(), event.getEntityType());
    }
    ParticleUtils.spawnParticleCylinder(Challenges.getInstance(), event.getEntity().getLocation(),
      Particle.ENTITY_EFFECT, 2, 17, 1);
  }

}
