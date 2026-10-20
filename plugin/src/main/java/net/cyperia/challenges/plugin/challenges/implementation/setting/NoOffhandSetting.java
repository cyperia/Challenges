package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class NoOffhandSetting extends Setting {

  @Override
  protected void onEnable() {
    broadcastFiltered(player -> {
      ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
      if (itemInOffHand.getType() == Material.AIR) return;
      player.getWorld().dropItemNaturally(player.getLocation(), itemInOffHand);
      player.getInventory().setItemInOffHand(null);
      player.updateInventory();
    });
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getType() != InventoryType.PLAYER) return;
    if (event.getSlot() != 40) return;
    event.setCancelled(true);
  }

}
