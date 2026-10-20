package net.cyperia.commons.bukkit.utils.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtils {

  @NotNull
  public static Material getIcon(@NotNull Object value) {
    if (value instanceof Material material) {
      if (material.isItem() && !material.isAir()) return material;
      // blocks without an item of their own, like a kelp plant, show the item that places them
      Material placement = material.createBlockData().getPlacementMaterial();
      if (placement.isItem() && !placement.isAir()) return placement;
    }
    if (value instanceof EntityType type) {
      Material spawnEgg = Material.matchMaterial(type.name() + "_SPAWN_EGG");
      if (spawnEgg != null) return spawnEgg;
    }
    return Material.PAPER;
  }

  @NotNull
  public static Material convertFoodToCookedFood(@NotNull Material material) {
    try {
      return Material.valueOf("COOKED_" + material.name());
    } catch (Exception ex) {
      return material; // No cooked material is available
    }
  }

  public static boolean isObtainableInSurvival(@NotNull Material material) {
    String name = material.name();
    if (material.isAir()) return false;
    if (name.endsWith("_SPAWN_EGG")) return false;
    if (name.startsWith("INFESTED_")) return false;
    if (name.startsWith("LEGACY_")) return false; // Legacy items should not be obtainable
    switch (name) {
      case "CHAIN_COMMAND_BLOCK":
      case "REPEATING_COMMAND_BLOCK":
      case "COMMAND_BLOCK":
      case "COMMAND_BLOCK_MINECART":
      case "JIGSAW":
      case "STRUCTURE_BLOCK":
      case "STRUCTURE_VOID":
      case "BARRIER":
      case "BEDROCK":
      case "KNOWLEDGE_BOOK":
      case "DEBUG_STICK":
      case "END_PORTAL_FRAME":
      case "END_PORTAL":
      case "NETHER_PORTAL":
      case "END_GATEWAY":
      case "LAVA":
      case "WATER":
      case "TALL_SEAGRASS":
      case "DIRT_PATH":
      case "CHORUS_PLANT":
      case "PETRIFIED_OAK_SLAB":
      case "FARMLAND":
      case "PLAYER_HEAD":
      case "GLOBE_BANNER_PATTERN":
      case "SPAWNER":
      case "BUDDING_AMETHYST":
      case "POWDER_SNOW":
      case "LIGHT":
      case "REINFORCED_DEEPSLATE":
      case "FROGSPAWN":
      case "SUSPICIOUS_GRAVEL":
      case "SUSPICIOUS_SAND":
      case "TEST_BLOCK":
      case "TEST_INSTANCE_BLOCK":
      case "VAULT":
      case "TRIAL_SPAWNER":
        return false;
    }

    return true;
  }

  public static boolean isBlockAvailableInSurvival(@NotNull Material material) {
    if (!material.isBlock()) return false;
    String name = material.name();
    if (material.isAir()) return false;
    if (name.startsWith("LEGACY_")) return false; // Legacy items should not be obtainable
    switch (name) {
      case "CHAIN_COMMAND_BLOCK":
      case "REPEATING_COMMAND_BLOCK":
      case "COMMAND_BLOCK":
      case "COMMAND_BLOCK_MINECART":
      case "JIGSAW":
      case "STRUCTURE_BLOCK":
      case "STRUCTURE_VOID":
      case "BARRIER":
      case "KNOWLEDGE_BOOK":
      case "DEBUG_STICK":
      case "END_PORTAL":
      case "NETHER_PORTAL":
      case "END_GATEWAY":
      case "PETRIFIED_OAK_SLAB":
      case "PLAYER_HEAD":
      case "GLOBE_BANNER_PATTERN":
      case "LIGHT":
      case "BUNDLE":
      case "TEST_BLOCK":
      case "TEST_INSTANCE_BLOCK":
        return false;
    }

    return true;
  }

  public static void damageItem(@NotNull ItemStack item) {
    damageItem(item, 1);
  }

  public static void damageItem(@NotNull ItemStack item, int amount) {
    Damageable meta = getDamageableMeta(item);
    if (meta == null) return;
    meta.setDamage(meta.getDamage() + amount);
    item.setItemMeta(meta);
  }

  /**
   * @return whether the tool survived the damage
   */
  public static boolean damageTool(@NotNull Player player, int slot) {
    PlayerInventory inventory = player.getInventory();
    ItemStack tool = inventory.getItem(slot);
    if (tool == null) return false;
    Damageable meta = getDamageableMeta(tool);
    if (meta == null) return true;

    int damage = meta.getDamage() + 1;
    if (damage >= tool.getType().getMaxDurability()) {
      inventory.setItem(slot, null);
      player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
      return false;
    }

    meta.setDamage(damage);
    tool.setItemMeta(meta);
    return true;
  }

  // Paper hands every item a Damageable meta, including ones without durability (max 0)
  @Nullable
  public static Damageable getDamageableMeta(@NotNull ItemStack item) {
    if (item.getType().getMaxDurability() <= 0) return null;
    return item.getItemMeta() instanceof Damageable damageable ? damageable : null;
  }

}
