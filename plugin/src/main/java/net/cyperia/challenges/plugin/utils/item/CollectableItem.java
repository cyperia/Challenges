package net.cyperia.challenges.plugin.utils.item;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * An item to find, once per material. With variants, enchanted books count once per enchantment and level
 * instead, and potions, splash and lingering potions and tipped arrows once per potion type.
 */
public record CollectableItem(@NotNull Material material, @Nullable Enchantment enchantment, int level,
                              @Nullable PotionType potion) {

  private static final Set<Material> POTION_FORMS = EnumSet.of(
    Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
  // Only obtainable through the creative inventory and commands
  private static final Set<PotionType> UNOBTAINABLE_POTIONS = EnumSet.of(PotionType.LUCK);

  private static List<CollectableItem> obtainableInSurvival, obtainableInSurvivalWithVariants;

  @NotNull
  public static CollectableItem of(@NotNull Material material) {
    return new CollectableItem(material, null, 0, null);
  }

  /**
   * Every item that can be obtained in survival in a fixed order: the material order, with the variants of
   * one material sorted by key.
   */
  @NotNull
  public static List<CollectableItem> obtainableInSurvival(boolean variants) {
    if (variants) {
      if (obtainableInSurvivalWithVariants == null) obtainableInSurvivalWithVariants = load(true);
      return obtainableInSurvivalWithVariants;
    }
    if (obtainableInSurvival == null) obtainableInSurvival = load(false);
    return obtainableInSurvival;
  }

  @NotNull
  private static List<CollectableItem> load(boolean variants) {
    List<CollectableItem> items = new ArrayList<>();
    for (Material material : ExperimentalUtils.getMaterials()) {
      if (!material.isItem() || !ItemUtils.isObtainableInSurvival(material)) continue;
      if (!variants) {
        items.add(of(material));
      } else if (material == Material.ENCHANTED_BOOK) {
        for (Enchantment enchantment : enchantments()) {
          for (int level = enchantment.getStartLevel(); level <= enchantment.getMaxLevel(); level++) {
            items.add(new CollectableItem(material, enchantment, level, null));
          }
        }
      } else if (POTION_FORMS.contains(material)) {
        for (PotionType potion : potions()) {
          items.add(new CollectableItem(material, null, 0, potion));
        }
      } else {
        items.add(of(material));
      }
    }
    return List.copyOf(items);
  }

  @NotNull
  private static List<Enchantment> enchantments() {
    return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
      .filter(enchantment -> enchantment.getKey().getNamespace().equals(NamespacedKey.MINECRAFT))
      .sorted(Comparator.comparing(enchantment -> enchantment.getKey().toString()))
      .toList();
  }

  @NotNull
  private static List<PotionType> potions() {
    return EnumSet.complementOf(EnumSet.copyOf(UNOBTAINABLE_POTIONS)).stream()
      .sorted(Comparator.comparing(potion -> potion.getKey().toString()))
      .toList();
  }

  /**
   * Every collectable item this stack counts as, with and without variants: a book with several enchantments
   * counts as one book per enchantment and as a plain enchanted book.
   */
  @NotNull
  public static List<CollectableItem> of(@NotNull ItemStack item) {
    Material material = item.getType();
    List<CollectableItem> items = new ArrayList<>();
    if (material == Material.ENCHANTED_BOOK && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
      for (Map.Entry<Enchantment, Integer> stored : meta.getStoredEnchants().entrySet()) {
        items.add(new CollectableItem(material, stored.getKey(), stored.getValue(), null));
      }
    } else if (POTION_FORMS.contains(material) && item.getItemMeta() instanceof PotionMeta meta && meta.getBasePotionType() != null) {
      items.add(new CollectableItem(material, null, 0, meta.getBasePotionType()));
    }
    items.add(of(material));
    return items;
  }

  public boolean matches(@NotNull ItemStack item) {
    if (item.getType() != material) return false;
    if (enchantment != null) {
      return item.getItemMeta() instanceof EnchantmentStorageMeta meta && meta.getStoredEnchantLevel(enchantment) == level;
    }
    if (potion != null) {
      return item.getItemMeta() instanceof PotionMeta meta && meta.getBasePotionType() == potion;
    }
    return true;
  }

  /**
   * {@code minecraft:stone}, {@code minecraft:enchanted_book[minecraft:protection=4]} or
   * {@code minecraft:potion[minecraft:long_swiftness]}, read back by {@link #parse(String)}.
   */
  @NotNull
  public String id() {
    String id = material.getKey().toString();
    if (enchantment != null) return id + "[" + enchantment.getKey() + "=" + level + "]";
    if (potion != null) return id + "[" + potion.getKey() + "]";
    return id;
  }

  /**
   * Reads an {@link #id()}, and the bare material names ({@code STONE}) saved before variants existed.
   *
   * @return {@code null} if the material, enchantment or potion is unknown to this version
   */
  @Nullable
  public static CollectableItem parse(@NotNull String id) {
    int variantStart = id.indexOf('[');
    Material material = Material.matchMaterial(variantStart < 0 ? id : id.substring(0, variantStart));
    if (material == null) return null;
    if (variantStart < 0 || !id.endsWith("]")) return of(material);

    String variant = id.substring(variantStart + 1, id.length() - 1);
    int levelStart = variant.lastIndexOf('=');
    if (levelStart >= 0) {
      NamespacedKey key = NamespacedKey.fromString(variant.substring(0, levelStart));
      Enchantment enchantment = key == null ? null : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
      if (enchantment == null) return null;
      try {
        return new CollectableItem(material, enchantment, Integer.parseInt(variant.substring(levelStart + 1)), null);
      } catch (NumberFormatException ex) {
        return null;
      }
    }
    NamespacedKey key = NamespacedKey.fromString(variant);
    for (PotionType potion : PotionType.values()) {
      if (potion.getKey().equals(key)) return new CollectableItem(material, null, 0, potion);
    }
    return null;
  }

  @NotNull
  public ItemStack icon() {
    ItemStack icon = new ItemStack(material);
    if (enchantment != null && icon.getItemMeta() instanceof EnchantmentStorageMeta meta) {
      meta.addStoredEnchant(enchantment, level, true);
      icon.setItemMeta(meta);
    } else if (potion != null && icon.getItemMeta() instanceof PotionMeta meta) {
      meta.setBasePotionType(potion);
      icon.setItemMeta(meta);
    }
    return icon;
  }

  // Translated by the viewer's own client, so the name follows their language
  @NotNull
  public LocalizableMessage displayName() {
    if (enchantment != null) {
      return MessageKey.of("challenge.generic.item-variant.enchanted-book").withArgs(material, enchantmentName());
    }
    if (potion == null) return LocalizableMessage.wrap(material);

    String key = potion.getKey().getKey();
    String base = key.replaceFirst("^(long|strong)_", "");
    Component name = Component.translatable("item.minecraft." + material.getKey().getKey() + ".effect." + base);
    if (key.startsWith("long_")) return MessageKey.of("challenge.generic.item-variant.long").withArgs(name);
    if (key.startsWith("strong_")) return MessageKey.of("challenge.generic.item-variant.strong").withArgs(name);
    return LocalizableMessage.wrap(name);
  }

  // Enchantment#displayName comes coloured like item lore; this one takes the colour of the text around it
  @NotNull
  private Component enchantmentName() {
    Component name = Component.translatable(enchantment.translationKey());
    if (enchantment.getMaxLevel() == 1) return name;
    return name.append(Component.space()).append(Component.translatable("enchantment.level." + level));
  }

  @Override
  public String toString() {
    return id().toLowerCase(Locale.ROOT);
  }

}
