package net.cyperia.commons.bukkit.utils.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class MenuPositionListener implements Listener {

  public MenuPositionListener() {
    MenuPosition.Holder.positions.clear();
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClick(@NotNull InventoryClickEvent event) {
    HumanEntity human = event.getWhoClicked();
    if (!(human instanceof Player player)) return;

    Inventory inventory = event.getClickedInventory();
    if (inventory == null) return;

    if (inventory == event.getInventory()) {
      if (inventory.getHolder() != MenuPosition.HOLDER) return; // No menu inventory

      MenuPosition position = MenuPosition.get(player);
      if (position == null) return; // Currently in no menu

      event.setCancelled(true);
      position.handleClick(new MenuClickInfo(player, inventory, event.isShiftClick(), event.isRightClick(), event.getSlot()));

    } else if (event.isShiftClick()) { // Player inventory was clicked
      Inventory topInventory = event.getInventory();
      if (topInventory.getHolder() != MenuPosition.HOLDER) return; // No menu inventory

      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClose(@NotNull InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player)) return;
    if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return; // New menu inventory
    if (event.getInventory().getHolder() != MenuPosition.HOLDER) return; // No menu inventory
    MenuPosition.remove(player);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onQuit(@NotNull PlayerQuitEvent event) {
    MenuPosition.remove(event.getPlayer());
  }

}
