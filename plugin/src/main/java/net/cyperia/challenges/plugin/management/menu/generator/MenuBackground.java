package net.cyperia.challenges.plugin.management.menu.generator;

import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class MenuBackground {

  private MenuBackground() {
  }

  public static void fill(@NotNull Inventory inventory) {
    InventoryUtils.fillInventory(inventory, DefaultItems.FILL_ITEM);
    InventoryUtils.fillInventory(inventory, DefaultItems.FILL_ITEM_CONTRAST, contrastSlots(inventory.getSize()));
  }

  static int[] contrastSlots(int size) {
    int lastRow = size / 9 - 1;
    if (lastRow < 2) return new int[0];
    return new int[]{
      0, 1, 2, 9,
      lastRow * 9 + 6, lastRow * 9 + 7, lastRow * 9 + 8, (lastRow - 1) * 9 + 8
    };
  }

}
