package net.cyperia.challenges.plugin.management.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An item of the hotbar players get while the timer is paused. Where it goes is decided by the
 * {@link HotbarLayout} chosen for the player; other plugins add items through
 * {@link HotbarManager#register(HotbarItem)}.
 */
public interface HotbarItem {

  /**
   * Names this item in {@code hotbar-items.yml} and is stamped into every stack it renders, so it
   * must be stable across restarts.
   */
  @NotNull
  String getId();

  /**
   * Whether the player may get this item right now, e.g. a permission or a setting.
   */
  boolean isAvailable(@NotNull Player player);

  /**
   * Whether the item is only handed out before a run, i.e. while the inventory holds nothing but
   * lobby items. In a paused run the free slots belong to the run.
   */
  default boolean isLobbyOnly() {
    return false;
  }

  /**
   * @return the stack, or {@code null} to keep the slot free for a stack another plugin places
   */
  @Nullable
  ItemStack render(@NotNull Player player);

  default void onUse(@NotNull Player player) {
  }

}
