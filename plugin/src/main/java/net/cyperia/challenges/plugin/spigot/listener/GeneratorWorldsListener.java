package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.server.GeneratorWorldPortalManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class GeneratorWorldsListener implements Listener {

  @EventHandler(priority = EventPriority.LOW)
  public void onChangeWorld(PlayerTeleportEvent event) {
    if (event.getTo() == null) return;

    GeneratorWorldPortalManager worldManager = Challenges.getInstance().getGeneratorWorldPortalManager();
    Player player = event.getPlayer();
    World from = event.getFrom().getWorld();
    World to = event.getTo().getWorld();
    if (from == null || to == null) return;
    if (from == to) return;

    boolean wasCustomWorld = worldManager.isCustomWorld(from.getName());
    boolean isCustomWorld = worldManager.isCustomWorld(to.getName());

    if (wasCustomWorld && !isCustomWorld) {

      if (to.getEnvironment() == Environment.NETHER || to.getEnvironment() == Environment.THE_END) {
        worldManager.setLastLocation(player, event.getFrom());
      }

    } else if (!wasCustomWorld) {

      if (event.getCause() != TeleportCause.END_PORTAL && event.getCause() != TeleportCause.NETHER_PORTAL) {
        return;
      }

      if (from.getEnvironment() == Environment.NETHER || from.getEnvironment() == Environment.THE_END) {
        Location location = worldManager.getAndRemoveLastWorld(player);
        if (location != null) {
          location = location.getBlock().getLocation().clone();
          event.setTo(location);
        }
      }

    }

  }

}
