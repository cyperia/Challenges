package net.cyperia.commons.bukkit.utils.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

class MenuPositionHolder implements InventoryHolder {

  @NotNull
  @Override
  public Inventory getInventory() {
    throw new UnsupportedOperationException("MenuPositionHolder does not hold an inventory");
  }

}
