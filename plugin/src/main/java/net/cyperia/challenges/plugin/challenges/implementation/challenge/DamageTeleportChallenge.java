package net.cyperia.challenges.plugin.challenges.implementation.challenge;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class DamageTeleportChallenge extends SettingModifier {

  private static final int PLAYER = 1, EVERYONE = 2;
  // A world without a border is 60 million blocks wide, which would put the target out of reach.
  private static final double MAX_RADIUS = 2000;

  private final Set<UUID> teleportPending = ConcurrentHashMap.newKeySet();

  public DamageTeleportChallenge() {
    super(1, 2);
    setValueUnit(ChallengeHelper.createAudienceUnit(EVERYONE));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    if (!shouldExecuteEffect()) return;
    if (ChallengeHelper.finalDamageIsNull(event)) return;

    handleDamage(((Player) event.getEntity()));
  }

  @EventHandler
  public void onQuit(@NotNull PlayerQuitEvent event) {
    teleportPending.remove(event.getPlayer().getUniqueId());
  }

  void handleDamage(@NotNull Player player) {
    // damage over time would otherwise queue teleports that land in a burst
    if (!teleportPending.add(player.getUniqueId())) return;

    World world = player.getWorld();
    Location random = getRandomLocation(world);

    Challenges.getInstance().getWorldManager().loadChunkAfterPrefetch(random, chunk -> {
      if (!shouldExecuteEffect() || !player.isValid() || player.isDead()) return;

      Location location = world.getHighestBlockAt(random).getLocation();
      location.setY(location.getY() + 1);

      if (getValue() == PLAYER) {
        Regions.entity(Challenges.getInstance(), player, () -> player.teleportAsync(location));
      } else {
        broadcastFiltered(target -> Regions.entity(Challenges.getInstance(), target, () -> target.teleportAsync(location)));
      }
    }).whenComplete((result, ex) -> teleportPending.remove(player.getUniqueId()));
  }

  public Location getRandomLocation(World world) {
    double size = Math.min(Challenges.getInstance().getBorderManager().getSize(world) / 2 - 1, MAX_RADIUS);

    final double randomX = ThreadLocalRandom.current().nextDouble(-size, size);
    final double randomY = ThreadLocalRandom.current().nextDouble(-size, size);

    return Challenges.getInstance().getBorderManager().getCenter(world).add(randomX, 0, randomY);
  }

}
