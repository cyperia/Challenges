package net.cyperia.challenges.plugin.utils.misc;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class InventoryUtils {

  private static final IRandom random;
  private static final List<Material> items;

  static {
    random = IRandom.create();
    items = new ArrayList<>(Arrays.asList(ExperimentalUtils.getMaterials()));
    items.removeIf(material -> !material.isItem());
  }

  private InventoryUtils() {
  }

  public static void fillInventory(@NotNull Inventory inventory, @Nullable ItemStack item) {
    for (int i = 0; i < inventory.getSize(); i++) {
      inventory.setItem(i, item);
    }
  }

  public static void fillInventory(@NotNull Inventory inventory, @Nullable ItemStack item, @NotNull int... slots) {
    for (int i : slots) {
      inventory.setItem(i, item);
    }
  }

  public static boolean isEmpty(@NotNull Inventory inventory) {
    for (ItemStack content : inventory.getContents()) {
      if (content != null) return false;
    }
    return true;
  }

  public static int getRandomEmptySlot(@NotNull Inventory inventory) {
    List<Integer> emptySlots = new ArrayList<>();

    for (int slot = 0; slot < inventory.getSize(); slot++) {
      if (inventory.getItem(slot) == null) {
        emptySlots.add(slot);
      }

    }

    if (emptySlots.isEmpty()) return -1;
    return emptySlots.get(ThreadLocalRandom.current().nextInt(emptySlots.size()));
  }

  public static int getRandomFullSlot(@NotNull Inventory inventory) {
    List<Integer> fullSlots = new ArrayList<>();

    for (int slot = 0; slot < inventory.getSize(); slot++) {
      ItemStack item = inventory.getItem(slot);
      if (item != null && !item.isSimilar(DefaultItems.BLOCKED_ITEM)) {
        fullSlots.add(slot);
      }
    }

    if (fullSlots.isEmpty()) return -1;

    return fullSlots.get(ThreadLocalRandom.current().nextInt(fullSlots.size()));
  }

  public static int getRandomSlot(@NotNull Inventory inventory) {
    List<Integer> slots = new ArrayList<>();

    for (int slot = 0; slot < inventory.getSize(); slot++) {
      ItemStack item = inventory.getItem(slot);
      if (item != null && item.isSimilar(DefaultItems.BLOCKED_ITEM)) continue;
      slots.add(slot);

    }

    if (slots.isEmpty()) return -1;
    return slots.get(ThreadLocalRandom.current().nextInt(slots.size()));
  }

  public static void dropItemByPlayer(@NotNull Location location, @NotNull ItemStack itemStack) {
    if (location.getWorld() == null) return;
    Item droppedItem = location.getWorld().dropItem(location.clone().add(0, 1.4, 0), itemStack);
    droppedItem.setVelocity(location.getDirection().multiply(0.4));
  }

  public static void dropOrGiveItem(@NotNull Inventory inventory, @NotNull Location location, @NotNull Material material) {
    dropOrGiveItem(inventory, location, new ItemStack(material));
  }

  public static void dropOrGiveItem(@NotNull Inventory inventory, @NotNull Location location, @NotNull ItemStack itemStack) {
    location = location.clone();
    if (inventory.firstEmpty() == -1) {
      if (location.getWorld() == null)
        location.setWorld(ChallengeAPI.getGameWorld(Environment.NORMAL));
      location.getWorld().dropItem(location, itemStack);
      return;
    }
    inventory.addItem(itemStack);
  }

  public static void removeRandomItem(@NotNull Inventory inventory) {
    int slot = InventoryUtils.getRandomFullSlot(inventory);
    if (slot == -1) return;
    inventory.setItem(slot, null);
  }

  public static void giveItem(@NotNull Player player, @NotNull ItemStack itemStack) {
    giveItem(player.getInventory(), player.getLocation(), itemStack);
  }

  public static void giveItem(@NotNull Inventory inventory, @NotNull Location locationToDrop, @NotNull ItemStack itemStack) {
    if (inventory.firstEmpty() == -1) {
      dropItemByPlayer(locationToDrop, itemStack);
      return;
    }
    inventory.addItem(itemStack);
  }

  public static ItemStack getRandomItem(boolean onlyOne, boolean respectMaxStackSize) {
    Material material = random.choose(items);
    int stackSize = onlyOne ? 1 : random.range(1, (respectMaxStackSize ? material.getMaxStackSize() : 64) + 1);
    return new ItemStack(material, stackSize);
  }

}
