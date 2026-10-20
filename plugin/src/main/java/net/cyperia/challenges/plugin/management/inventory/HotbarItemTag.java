package net.cyperia.challenges.plugin.management.inventory;

import net.cyperia.challenges.plugin.Challenges;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Identifies hotbar stacks by a tag in their persistent data. Display name and material both change
 * with the player's language and the config, so neither can tell a hotbar item from a run item.
 */
public final class HotbarItemTag {

  private static final NamespacedKey KEY = new NamespacedKey(Challenges.getInstance(), "hotbar_item");

  private HotbarItemTag() {
  }

  @NotNull
  public static ItemStack tag(@NotNull ItemStack stack, @NotNull String id) {
    ItemMeta meta = stack.getItemMeta();
    if (meta == null) return stack;
    meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, id);
    stack.setItemMeta(meta);
    return stack;
  }

  /**
   * @return the id of the {@link HotbarItem} the stack was rendered by, or {@code null} for any other stack
   */
  @Nullable
  public static String idOf(@Nullable ItemStack stack) {
    if (stack == null || !stack.hasItemMeta()) return null;
    ItemMeta meta = stack.getItemMeta();
    return meta == null ? null : meta.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
  }

  public static boolean isTagged(@Nullable ItemStack stack) {
    return idOf(stack) != null;
  }

}
