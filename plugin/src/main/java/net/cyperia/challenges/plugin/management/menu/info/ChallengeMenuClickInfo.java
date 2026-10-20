package net.cyperia.challenges.plugin.management.menu.info;

import lombok.ToString;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

@ToString
public class ChallengeMenuClickInfo extends MenuClickInfo {

  protected final boolean upperItem;

  public ChallengeMenuClickInfo(@NotNull MenuClickInfo parent, boolean upperItem) {
    this(parent.getPlayer(), parent.getInventory(), parent.isShiftClick(), parent.isRightClick(), parent.getSlot(), upperItem);
  }

  public ChallengeMenuClickInfo(@NotNull Player player, @NotNull Inventory inventory, boolean shiftClick, boolean rightClick, int slot, boolean upperItem) {
    super(player, inventory, shiftClick, rightClick, slot);
    this.upperItem = upperItem;
  }

  public boolean isUpperItemClick() {
    return upperItem;
  }

  public boolean isLowerItemClick() {
    return !upperItem;
  }

}
