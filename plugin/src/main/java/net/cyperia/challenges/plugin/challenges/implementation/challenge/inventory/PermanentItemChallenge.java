package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class PermanentItemChallenge extends Setting {

  @EventHandler(priority = EventPriority.HIGH)
  public void onInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;
    Inventory clickedInventory = event.getClickedInventory();
    if (event.getCursor() == null) return;
    if (clickedInventory == null) return;
    InventoryType type = player.getOpenInventory().getTopInventory().getType();
    if (type == InventoryType.WORKBENCH || type == InventoryType.CRAFTING) return;
    if (clickedInventory.getType() == InventoryType.CRAFTING) return;
    if (clickedInventory.getType() == InventoryType.PLAYER) {
      if (event.getInventory().getType() != InventoryType.PLAYER) {
        event.setCancelled(true);
      }
    }

  }

  @EventHandler
  public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
    if (!shouldExecuteEffect()) return;
    event.setCancelled(true);
  }

}
