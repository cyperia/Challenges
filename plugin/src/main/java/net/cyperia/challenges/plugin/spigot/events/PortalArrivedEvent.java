package net.cyperia.challenges.plugin.spigot.events;

import org.bukkit.Bukkit;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * An entity that has arrived in another world through a portal. Called on the thread that owns the
 * entity at its destination, where it can be moved on.
 */
public class PortalArrivedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Entity entity;
  private final World from;
  private final PortalType portalType;

  // Bukkit accepts a sync event on a region thread only, and an async one everywhere else
  public PortalArrivedEvent(@NotNull Entity entity, @NotNull World from, @NotNull PortalType portalType) {
    super(!Bukkit.isPrimaryThread());
    this.entity = entity;
    this.from = from;
    this.portalType = portalType;
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
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
