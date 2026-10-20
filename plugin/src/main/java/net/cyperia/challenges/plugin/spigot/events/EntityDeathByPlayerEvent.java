package net.cyperia.challenges.plugin.spigot.events;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDeathByPlayerEvent extends EntityEvent implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  @Getter
  private final Player killer;
  private final Cancellable parentEvent;

  public EntityDeathByPlayerEvent(@NotNull Entity victim, Player killer, Cancellable parent) {
    super(victim);
    this.killer = killer;
    this.parentEvent = parent;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return parentEvent.isCancelled();
  }

  @Override
  public void setCancelled(boolean cancel) {
    parentEvent.setCancelled(cancel);
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
