package net.cyperia.commons.bukkit.utils.menu;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
@EqualsAndHashCode
public class MenuClickInfo {

  protected final Player player;
  protected final Inventory inventory;
  protected final boolean shiftClick;
  protected final boolean rightClick;
  protected final int slot;

  public MenuClickInfo(@NotNull Player player, @NotNull Inventory inventory, boolean shiftClick, boolean rightClick, int slot) {
    this.player = player;
    this.inventory = inventory;
    this.shiftClick = shiftClick;
    this.rightClick = rightClick;
    this.slot = slot;
  }

  public boolean isLeftClick() {
    return !rightClick;
  }

  @Nullable
  public ItemStack getClickedItem() {
    return inventory.getItem(slot);
  }

  @NotNull
  public Material getClickedMaterial() {
    ItemStack item = getClickedItem();
    return item == null ? Material.AIR : item.getType();
  }

  @NotNull
  public MenuClickType getType() {
    return MenuClickType.of(rightClick, shiftClick);
  }

}
