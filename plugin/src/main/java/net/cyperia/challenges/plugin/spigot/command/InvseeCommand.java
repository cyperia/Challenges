package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import net.cyperia.commons.bukkit.utils.menu.positions.SlottedMenuPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InvseeCommand implements PlayerCommand, Listener {

  private static final int
    startBottom = 36,
    endBottom = 53,
    helmetSlot = 46,
    chestPlateSlot = 47,
    leggingsSlot = 48,
    bootsSlot = 49,
    offHandSlot = 52;

  private final Map<Player, Inventory> inventories = new HashMap<>();

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
    if (args.length < 1) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "invsee <player>");
      return;
    }

    Player target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      MessageKey.of("command.no-target").send(player, Prefix.CHALLENGES);
      return;
    }

    player.openInventory(getInventory(target));
    MenuPosition.set(player, new SlottedMenuPosition());
    MessageKey.of("command.invsee.open").send(player, Prefix.CHALLENGES, target);
  }

  public Inventory getInventory(@NotNull Player target) {
    if (inventories.containsKey(target)) {
      return inventories.get(target);
    }

    Inventory inventory = Bukkit.createInventory(MenuPosition.HOLDER, 6 * 9, MessageKey.of("command.invsee.title").asComponent(target, target));
    inventories.put(target, inventory);
    MenuPosition.setEmpty(target);
    updateInventoryContents(inventory, target.getInventory());
    return inventory;
  }

  public void updateInventoryContents(@NotNull Inventory inventory, @NotNull PlayerInventory playerInventory) {
    inventory.clear();

    for (int slot = startBottom; slot <= endBottom; slot++) {
      inventory.setItem(slot, DefaultItems.FILL_ITEM);
    }

    inventory.setItem(helmetSlot, playerInventory.getHelmet());
    inventory.setItem(chestPlateSlot, playerInventory.getChestplate());
    inventory.setItem(leggingsSlot, playerInventory.getLeggings());
    inventory.setItem(bootsSlot, playerInventory.getBoots());
    inventory.setItem(offHandSlot, playerInventory.getItemInOffHand());

    for (int slot = 0; slot < 36; slot++) {
      inventory.setItem(slot, playerInventory.getItem(slot));
    }
  }

  public void updateInventory(@NotNull Player player) {
    if (!inventories.containsKey(player)) return;
    Inventory inventory = inventories.get(player);
    updateInventoryContents(inventory, player.getInventory());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getHolder() != event.getPlayer()) return;
    Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> updateInventory(event.getPlayer()), 1);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
    Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> updateInventory(event.getPlayer()), 1);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    Inventory inventory = inventories.remove(event.getPlayer());
    if (inventory == null) return;
    inventory.close();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onClose(@NotNull InventoryCloseEvent event) {
    if (event.getInventory().getHolder() != MenuPosition.HOLDER) return;
    Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), this::pruneStaleInventories, 1);
  }

  private void pruneStaleInventories() {
    inventories.values().removeIf(inventory -> inventory.getViewers().isEmpty());
  }

}
