package net.cyperia.challenges.plugin.management.menu.generator.impl.timer;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public abstract class AbstractTimeUnitMenuGenerator extends AbstractTimerMenuGenerator {

  public static final int SHIFT_CHANGE_TIME_AMOUNT = 10;

  protected static final List<TimeUnit> TIME_UNITS = List.of(
    new TimeUnit(24 * 60 * 60, 0, "days", Material.GOLD_BLOCK),
    new TimeUnit(60 * 60, 24, "hours", Material.GOLD_INGOT),
    new TimeUnit(60, 60, "minutes", Material.RAW_GOLD),
    new TimeUnit(1, 60, "seconds", Material.GOLD_NUGGET)
  );

  /**
   * One unit the displayed duration is split into.
   *
   * @param wrapAt the amount this unit rolls over at, {@code 0} for the largest one
   */
  protected record TimeUnit(int seconds, int wrapAt, @NotNull String name, @NotNull Material material) {

    long amountOf(long totalSeconds) {
      long amount = totalSeconds / seconds;
      return wrapAt == 0 ? amount : amount % wrapAt;
    }

    @NotNull
    MessageKey nameKey() {
      return MessageKey.of("menu.timer.unit." + name);
    }
  }

  /**
   * @return the slot the item of every {@link #TIME_UNITS} entry sits at, in the same order
   */
  protected abstract int[] getUnitSlots();

  /**
   * @return the duration currently displayed, in seconds
   */
  protected abstract long getTime();

  protected abstract void setUnitItems(@NotNull Inventory inventory, @NotNull TimeUnit unit, int slot, long amount,
                                       @NotNull String formattedTime, @NotNull Locale locale);

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, @NotNull Locale locale) {
    long time = getTime();
    String formattedTime = TimerFormat.SIMPLE_FORMAT.format(time);
    int[] slots = getUnitSlots();

    for (int i = 0; i < TIME_UNITS.size(); i++) {
      TimeUnit unit = TIME_UNITS.get(i);
      setUnitItems(inventory, unit, slots[i], unit.amountOf(time), formattedTime, locale);
    }
  }

  /**
   * @return the index into {@link #TIME_UNITS} whose item sits at the given slot, or {@code -1}
   */
  protected int findUnitIndex(int slot) {
    int[] slots = getUnitSlots();
    for (int i = 0; i < slots.length; i++) {
      if (slots[i] == slot) return i;
    }
    return -1;
  }

  protected int getChangeAmount(@NotNull TimeUnit unit, boolean add, boolean shiftClick) {
    int amount = unit.seconds() * (shiftClick ? SHIFT_CHANGE_TIME_AMOUNT : 1);
    return add ? amount : -amount;
  }

  @NotNull
  protected ItemFormatter createTimeItem(@NotNull TimeUnit unit, @NotNull Material material, long amount, @NotNull String formattedTime) {
    return new ItemFormatter(material)
      .name(MessageKey.of("menu.timer.state.time").withArgs(unit.nameKey(), amount, formattedTime));
  }

  @NotNull
  protected ItemStack buildUnitItem(@NotNull ItemFormatter item, long amount, @NotNull Locale locale) {
    return item.builder(locale)
      .setAmount((int) Math.max(amount, 1))
      .build();
  }

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    MenuType.TIMER.getMenuGenerator().openMenu(player);
  }
}
