package net.cyperia.challenges.plugin.management.menu.generator.impl.timer;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.menu.generator.SinglePageMenuGenerator;
import net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * The frame every timer menu shares: its size, its decoration and the permission it guards its clicks
 * with.
 */
public abstract class AbstractTimerMenuGenerator extends SinglePageMenuGenerator {

  public static final int SIZE = 5 * 9;
  public static final String MANAGE_PERMISSION = "challenges.timer";

  private static final int[] CONTRAST_SLOTS = {1, 2, 6, 7, 9, 10, 16, 17, 27, 28, 34, 35, 37, 38, 39, 41, 42, 43};

  @NotNull
  protected static ChallengeTimer getTimer() {
    return Challenges.getInstance().getChallengeTimer();
  }

  @Override
  public void setInventoryDecoration(@NotNull Inventory inventory, @NotNull Locale locale) {
    InventoryUtils.fillInventory(inventory, DefaultItems.FILL_ITEM); // its own contrast pattern replaces the corner glass
    for (int slot : CONTRAST_SLOTS) {
      inventory.setItem(slot, DefaultItems.FILL_ITEM_CONTRAST);
    }
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  /**
   * @return {@code true} if the player may not manage the timer; the rejection effect has then already
   * been played and the click must not be handled
   */
  protected boolean playNoPermissionsEffect(@NotNull Player player) {
    if (player.hasPermission(MANAGE_PERMISSION)) return false;
    Challenges.getInstance().getMenuManager().playNoPermissionsEffect(player);
    return true;
  }

}
