package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RandomItemDroppingChallenge extends TimedChallenge {

  public RandomItemDroppingChallenge() {
    super(1, 60, 5);
    setValueUnit(ValueUnit.INTERVAL_SECONDS);
  }

  public static void dropRandomItem(Player player) {
    if (player.getInventory().getContents().length == 0) return;
    dropRandomItem(player.getLocation(), player.getInventory());
  }

  public static void dropRandomItem(@NotNull Location location, @NotNull Inventory inventory) {
    if (location.getWorld() == null) return;
    int slot = InventoryUtils.getRandomFullSlot(inventory);
    if (slot == -1) return;
    ItemStack item = inventory.getItem(slot);
    if (item == null) return;
    inventory.setItem(slot, null);
    InventoryUtils.dropItemByPlayer(location, item);
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();

    Bukkit.getScheduler().runTask(plugin, () -> {
      broadcastFiltered(RandomItemDroppingChallenge::dropRandomItem);
    });
  }

}
