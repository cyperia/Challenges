package net.cyperia.challenges.plugin.spigot.events;

import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerPickupItemEvent extends PlayerEvent implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final Item item;
  private final int remaining;
  private boolean cancel = false;

  public PlayerPickupItemEvent(@NotNull Player player, @NotNull Item item, int remaining) {
    super(player);
    this.item = item;
    this.remaining = remaining;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return cancel;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancel = cancel;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
