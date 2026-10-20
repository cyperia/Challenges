package net.cyperia.challenges.plugin.spigot.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerInventoryClickEvent extends InventoryClickEventWrapper {

  private static final HandlerList handlers = new HandlerList();

  private final Player player;

  public PlayerInventoryClickEvent(@NotNull InventoryClickEvent event) {
    super(event);
    player = (Player) event.getWhoClicked();
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return getHandlerList();
  }

}
