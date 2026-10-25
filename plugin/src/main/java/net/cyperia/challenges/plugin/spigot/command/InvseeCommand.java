package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.challenges.plugin.utils.misc.CommandHelper;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import net.cyperia.commons.bukkit.utils.menu.positions.SlottedMenuPosition;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvseeCommand implements ChallengeCommand, Listener {

  private static final int
    startBottom = 36,
    endBottom = 53,
    helmetSlot = 46,
    chestPlateSlot = 47,
    leggingsSlot = 48,
    bootsSlot = 49,
    offHandSlot = 52;

  private final Map<Player, Inventory> inventories = new ConcurrentHashMap<>();

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("invsee", ChallengesPermissions.COMMAND_INVSEE).asGeneric();
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.then(nodes.argument("player", StringArgumentType.word())
      .suggests(nodes.suggest(CommandHelper::getNames))
      .executes(nodes.runAsPlayer((player, context) -> {
        Player target = CommandHelper.getPlayer(player, StringArgumentType.getString(context, "player"));
        if (target == null) {
          MessageKey.of("command.no-target").send(player, Prefix.CHALLENGES);
          return;
        }

        // The target's inventory is read on the target's thread, the viewer opens it on theirs
        Regions.entity(Challenges.getInstance(), target, () -> {
          Inventory inventory = getInventory(target);
          Regions.entity(Challenges.getInstance(), player, () -> {
            player.openInventory(inventory);
            MenuPosition.set(player, new SlottedMenuPosition());
            MessageKey.of("command.invsee.open").send(player, Prefix.CHALLENGES, target);
          });
        });
      })));
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
    Regions.later(Challenges.getInstance(), event.getPlayer(), () -> updateInventory(event.getPlayer()), 1);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
    Regions.later(Challenges.getInstance(), event.getPlayer(), () -> updateInventory(event.getPlayer()), 1);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    Inventory inventory = inventories.remove(event.getPlayer());
    if (inventory == null) return;
    // Viewers sit in other regions than the quitting target, a window is closed on its viewer's thread
    for (HumanEntity viewer : List.copyOf(inventory.getViewers())) {
      Regions.entity(Challenges.getInstance(), viewer, viewer::closeInventory);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onClose(@NotNull InventoryCloseEvent event) {
    if (event.getInventory().getHolder() != MenuPosition.HOLDER) return;
    Regions.later(Challenges.getInstance(), this::pruneStaleInventories, 1);
  }

  private void pruneStaleInventories() {
    inventories.values().removeIf(inventory -> inventory.getViewers().isEmpty());
  }

}
