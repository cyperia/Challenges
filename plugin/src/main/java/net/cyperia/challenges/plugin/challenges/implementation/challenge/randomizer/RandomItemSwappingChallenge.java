package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RandomItemSwappingChallenge extends TimedChallenge {

  public RandomItemSwappingChallenge() {
    super(1, 60, 5);
    setValueUnit(ValueUnit.INTERVAL_SECONDS);
  }

  public static void swapRandomItems(Player player) {
    if (player.getInventory().getContents().length == 0) return;
    int slot = InventoryUtils.getRandomFullSlot(player.getInventory());
    if (slot == -1) return;
    swapItemToRandomSlot(
      player.getInventory(),
      InventoryUtils.getRandomFullSlot(player.getInventory()),
      InventoryUtils.getRandomSlot(player.getInventory())
    );
  }

  private static void swapItemToRandomSlot(@NotNull Inventory inventory, int slot1, int slot2) {
    if (slot1 == -1 || slot2 == -1) return;
    ItemStack item1 = inventory.getItem(slot1);
    ItemStack item2 = inventory.getItem(slot2);
    inventory.setItem(slot1, item2);
    inventory.setItem(slot2, item1);
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();

    Regions.nextTick(plugin, () -> {

      broadcastFiltered(RandomItemSwappingChallenge::swapRandomItems);

    });

  }

}
