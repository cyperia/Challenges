package net.cyperia.challenges.plugin.spigot.region;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.cyperia.challenges.plugin.spigot.events.PortalTravelEvent;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;


public final class BukkitRespawnPortalAdapter implements Listener {

  // RespawnSetting used to settle the location at MONITOR, so it outranks every other plugin's change
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onRespawn(@NotNull PlayerRespawnEvent event) {
    Location original = event.getRespawnLocation();
    Location settled = RespawnPortalEvents.respawnLocation(event.getPlayer(), original);
    if (settled != original) event.setRespawnLocation(settled);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPostRespawn(@NotNull PlayerPostRespawnEvent event) {
    RespawnPortalEvents.respawned(event.getPlayer());
  }

  @EventHandler
  public void onPlayerPortal(@NotNull PlayerPortalEvent event) {
    travel(event.getPlayer(), event.getFrom(), event.getTo(), portalType(event.getCause()), event.isCancelled(), event::setCancelled, event::setTo);
  }

  @EventHandler
  public void onEntityPortal(@NotNull EntityPortalEvent event) {
    travel(event.getEntity(), event.getFrom(), event.getTo(), event.getPortalType(), event.isCancelled(), event::setCancelled, event::setTo);
  }

  private static void travel(@NotNull Entity entity, @NotNull Location from, Location to, @NotNull PortalType type,
                             boolean cancelled, @NotNull Consumer<Boolean> setCancelled, @NotNull Consumer<Location> setTo) {
    if (to == null || to.getWorld() == null || from.getWorld() == null) return;

    PortalTravelEvent travel = RespawnPortalEvents.portalTravel(entity, from.getWorld(), to.getWorld(), type, to, cancelled);
    if (travel.isCancelled() != cancelled) setCancelled.accept(travel.isCancelled());
    if (travel.getDestination() != to) setTo.accept(travel.getDestination());
    // The server moves the entity once this handler is through, so it has arrived by its next tick
    if (!travel.isCancelled()) {
      Regions.later(Challenges.getInstance(), entity, () -> RespawnPortalEvents.portalArrived(entity, from.getWorld(), type), 1);
    }
  }

  @NotNull
  static PortalType portalType(@NotNull TeleportCause cause) {
    return switch (cause) {
      case NETHER_PORTAL -> PortalType.NETHER;
      case END_PORTAL -> PortalType.ENDER;
      case END_GATEWAY -> PortalType.END_GATEWAY;
      default -> PortalType.CUSTOM;
    };
  }

}
