package net.cyperia.challenges.plugin.spigot.region;

import io.canvasmc.canvas.event.EntityPortalAsyncEvent;
import io.canvasmc.canvas.event.EntityPostPortalAsyncEvent;
import io.canvasmc.canvas.event.PlayerPostRespawnAsyncEvent;
import io.canvasmc.canvas.event.PlayerRespawnAsyncEvent;
import net.cyperia.challenges.plugin.spigot.events.PortalTravelEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class CanvasRespawnPortalAdapter implements Listener {

  @EventHandler
  public void onRespawn(@NotNull PlayerRespawnAsyncEvent event) {
    Location original = event.getRespawnLocation();
    Location settled = RespawnPortalEvents.respawnLocation(event.getPlayer(), original);
    if (settled != original) event.setRespawnLocation(settled);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPostRespawn(@NotNull PlayerPostRespawnAsyncEvent event) {
    RespawnPortalEvents.respawned(event.getPlayer());
  }

  @EventHandler
  public void onPortal(@NotNull EntityPortalAsyncEvent event) {
    World target = event.getTo();
    PortalTravelEvent travel = RespawnPortalEvents.portalTravel(event.getEntity(), event.getFrom(), target,
      event.getPortalType(), null, event.isCancelled());
    if (travel.isCancelled() != event.isCancelled()) event.setCancelled(travel.isCancelled());
    if (travel.getTargetWorld() != target) event.setTo(travel.getTargetWorld());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPostPortal(@NotNull EntityPostPortalAsyncEvent event) {
    RespawnPortalEvents.portalArrived(event.getEntity(), event.getFrom(), event.getPortalType());
  }

}
