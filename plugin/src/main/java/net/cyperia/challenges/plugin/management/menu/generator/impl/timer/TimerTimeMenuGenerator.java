package net.cyperia.challenges.plugin.management.menu.generator.impl.timer;

import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.scheduler.policy.ExtraWorldPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TimerTimeMenuGenerator extends AbstractTimeUnitMenuGenerator {

  public static final int[] UNIT_SLOTS = {20, 21, 23, 24};
  public static final int CONTROL_SLOT_OFFSET = 9; // one row

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(super.getMenuName(), MessageKey.of("menu.timer.time.menu"));
  }

  /**
   * Shows the time running down or up, and picks up every change made outside this menu (a
   * {@code /timer set}, the API, a reset), so that nothing has to push to it.
   *
   * @implNote {@link TimerPolicy#ALWAYS} because the time is set while the timer is paused, and
   * {@link ExtraWorldPolicy#ALWAYS} because the default policy would stop the refresh while an extra
   * world is in use
   */
  @ScheduledTask(ticks = 20, timerPolicy = TimerPolicy.ALWAYS, worldPolicy = ExtraWorldPolicy.ALWAYS)
  public void updateTimeInventory() {
    updatePages();
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(@NotNull Player player) {
    return new TimerTimeMenuPosition(player);
  }

  @Override
  protected int[] getUnitSlots() {
    return UNIT_SLOTS;
  }

  @Override
  protected long getTime() {
    return getTimer().getTime();
  }

  @Override
  protected void setUnitItems(@NotNull Inventory inventory, @NotNull TimeUnit unit, int slot, long amount,
                              @NotNull String formattedTime, @NotNull Locale locale) {
    inventory.setItem(slot - CONTROL_SLOT_OFFSET, createControlItem(unit, true, amount, formattedTime, locale));
    inventory.setItem(slot, createResetItem(unit, amount, formattedTime, locale));
    inventory.setItem(slot + CONTROL_SLOT_OFFSET, createControlItem(unit, false, amount, formattedTime, locale));
  }

  @NotNull
  private ItemStack createControlItem(@NotNull TimeUnit unit, boolean add, long amount, @NotNull String formattedTime, @NotNull Locale locale) {
    Interaction interaction = add ? Interaction.ADD : Interaction.SUBTRACT;
    return createTimeItem(unit, add ? Material.DARK_OAK_BUTTON : Material.STONE_BUTTON, amount, formattedTime)
      .tooltip(MenuClickType.LEFT, interaction.withArgs(ArgumentFormat.TIME.apply(unit.seconds())))
      .tooltip(MenuClickType.LEFT_SHIFT, interaction.withArgs(ArgumentFormat.TIME.apply(unit.seconds() * SHIFT_CHANGE_TIME_AMOUNT)))
      .build(locale);
  }

  @NotNull
  private ItemStack createResetItem(@NotNull TimeUnit unit, long amount, @NotNull String formattedTime, @NotNull Locale locale) {
    return buildUnitItem(createTimeItem(unit, unit.material(), amount, formattedTime)
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.timer.interact.reset")), amount, locale);
  }

  public class TimerTimeMenuPosition extends HistoryAwareGeneratorMenuPosition {

    public TimerTimeMenuPosition(@NotNull Player player) {
      super(0, player);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      for (int i = 0; i < UNIT_SLOTS.length; i++) {
        int slot = UNIT_SLOTS[i];
        boolean add = info.getSlot() == slot - CONTROL_SLOT_OFFSET;
        boolean subtract = info.getSlot() == slot + CONTROL_SLOT_OFFSET;
        if (!add && !subtract && info.getSlot() != slot) continue;
        if (playNoPermissionsEffect(info.getPlayer())) return true;

        if (!add && !subtract) {
          getTimer().reset();
          SoundSample.BASS_OFF.play(info.getPlayer());
        } else {
          getTimer().addSeconds(getChangeAmount(TIME_UNITS.get(i), add, info.isShiftClick()));
          SoundSample.PLOP.play(info.getPlayer());
        }

        updatePages(); // the clicker should not have to wait for the periodic refresh
        return true;
      }
      return false;
    }

  }

}
