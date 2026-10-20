package net.cyperia.challenges.plugin.challenges.implementation.challenge;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class DamageTeleportChallenge extends SettingModifier {

  private static final int PLAYER = 1, EVERYONE = 2;

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

  private void handleDamage(@NotNull Player player) {

    Location location = player.getWorld().getHighestBlockAt(getRandomLocation(player.getWorld())).getLocation();
    location.setY(location.getY() + 1);
    location.getChunk().load(true);

    if (getValue() == PLAYER) {
      player.teleport(location);
    } else {
      broadcastFiltered(player1 -> player1.teleport(location));
    }

  }

  public Location getRandomLocation(World world) {
    double size = Challenges.getInstance().getBorderManager().getSize(world) / 2;
    size--;

    final double randomX = ThreadLocalRandom.current().nextDouble(-size, size);
    final double randomY = ThreadLocalRandom.current().nextDouble(-size, size);

    return Challenges.getInstance().getBorderManager().getCenter(world).add(randomX, 0, randomY);
  }

}
