package net.cyperia.challenges.plugin.spigot.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A player or entity about to travel through a portal. The destination in the target world is only
 * known on servers that hand it out (Paper); elsewhere the server picks it once the world is settled,
 * and {@link #getDestination()} is null.
 */
public class PortalTravelEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final Entity entity;
  private final World from;
  private final PortalType portalType;
  private World targetWorld;
  private Location destination;
  private boolean cancelled;

  // Bukkit accepts a sync event on a region thread only, and an async one everywhere else
  public PortalTravelEvent(@NotNull Entity entity, @NotNull World from, @NotNull World targetWorld, @NotNull PortalType portalType, @Nullable Location destination) {
    super(!Bukkit.isPrimaryThread());
    this.entity = entity;
    this.from = from;
    this.targetWorld = targetWorld;
    this.portalType = portalType;
    this.destination = destination;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @NotNull
  public Entity getEntity() {
    return entity;
  }

  @NotNull
  public World getFrom() {
    return from;
  }

  @NotNull
  public PortalType getPortalType() {
    return portalType;
  }

  @NotNull
  public World getTargetWorld() {
    return targetWorld;
  }

  // An existing destination is rescaled from the coordinates of the world it led to into the new one's
  public void setTargetWorld(@NotNull World targetWorld) {
    if (destination != null) {
      double ratio = this.targetWorld.getCoordinateScale() / targetWorld.getCoordinateScale();
      destination = destination.clone();
      destination.setWorld(targetWorld);
      destination.setX(destination.getX() * ratio);
      destination.setZ(destination.getZ() * ratio);
    }
    this.targetWorld = targetWorld;
  }

  @Nullable
  public Location getDestination() {
    return destination;
  }

  public void setDestination(@NotNull Location destination) {
    this.destination = destination;
    this.targetWorld = destination.getWorld();
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
