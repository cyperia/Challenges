package net.cyperia.challenges.plugin.management.menu.generator.impl.timer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.challenges.type.annotation.Since;
import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.scheduler.policy.ExtraWorldPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Since("3.0")
public class TimerCountdownMenuGenerator extends AbstractTimeUnitMenuGenerator {

  public static final int STATE_SLOT = 13;
  public static final int[] UNIT_SLOTS = {29, 30, 32, 33};

  /**
   * What the item at {@link #STATE_SLOT} shows, and whether clicking it does anything.
   */
  @Getter
  @RequiredArgsConstructor
  private enum CountdownState {

    RUNNING(Material.LIME_DYE, "running", "stop"),
    STOPPED(Material.RED_DYE, "stopped", "start"),
    UNSET(Material.GRAY_DYE, "unset", null),
    TIMER_STARTED(Material.GREEN_DYE, "timer-started", null);

    private final Material material;
    private final String stateKey;
    /** The interaction the item offers, {@code null} if the countdown cannot be toggled in this state. */
    @Nullable
    private final String interactionKey;

    boolean isToggleable() {
      return interactionKey != null;
    }
  }

  @NotNull
  private static CountdownState getCountdownState() {
    ChallengeTimer timer = getTimer();
    if (timer.isStarted()) return CountdownState.TIMER_STARTED;
    if (timer.isCountdownRunning()) return CountdownState.RUNNING;
    if (timer.getCountdown() <= 0) return CountdownState.UNSET;
    return CountdownState.STOPPED;
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(super.getMenuName(), MessageKey.of("menu.timer.countdown.menu"));
  }

  @ScheduledTask(ticks = 20, timerPolicy = TimerPolicy.COUNTDOWN, worldPolicy = ExtraWorldPolicy.ALWAYS)
  @TimerTask(status = {TimerStatus.PAUSED, TimerStatus.COUNTDOWN, TimerStatus.RUNNING}, worldPolicy = ExtraWorldPolicy.ALWAYS)
  public void updateCountdownInventory() {
    updatePages();
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(@NotNull Player player) {
    return new TimerCountdownMenuPosition(player);
  }

  @Override
  protected int[] getUnitSlots() {
    return UNIT_SLOTS;
  }

  @Override
  protected long getTime() {
    return getTimer().getCountdown();
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, @NotNull Locale locale) {
    super.updateInventoryContent(inventory, locale);
    inventory.setItem(STATE_SLOT, createStateItem(getCountdownState(), locale));
  }

  @Override
  protected void setUnitItems(@NotNull Inventory inventory, @NotNull TimeUnit unit, int slot, long amount,
                              @NotNull String formattedTime, @NotNull Locale locale) {
    ItemFormatter item = createTimeItem(unit, unit.material(), amount, formattedTime);
    for (MenuClickType clickType : MenuClickType.values()) {
      Interaction interaction = clickType.isRightClick() ? Interaction.SUBTRACT : Interaction.ADD;
      int seconds = unit.seconds() * (clickType.isShiftClick() ? SHIFT_CHANGE_TIME_AMOUNT : 1);
      item.tooltip(clickType, interaction.withArgs(ArgumentFormat.TIME.apply(seconds)));
    }
    inventory.setItem(slot, buildUnitItem(item, amount, locale));
  }

  @NotNull
  private ItemStack createStateItem(@NotNull CountdownState state, @NotNull Locale locale) {
    return new ItemFormatter(state.getMaterial())
      .name(MessageKey.of("menu.timer.countdown.state." + state.getStateKey()))
      .tooltipIf(state.isToggleable(), MenuClickType.LEFT, "menu.timer.countdown.interact." + state.getInteractionKey())
      .build(locale);
  }

  public class TimerCountdownMenuPosition extends HistoryAwareGeneratorMenuPosition {

    public TimerCountdownMenuPosition(@NotNull Player player) {
      super(0, player);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      if (info.getSlot() == STATE_SLOT) {
        if (playNoPermissionsEffect(info.getPlayer(), ChallengesPermissions.COMMAND_TIMER_COUNTDOWN)) return true;
        if (!getCountdownState().isToggleable()) {
          SoundSample.BASS_OFF.play(info.getPlayer());
          return true;
        }
        getTimer().toggleCountdown();
        updatePages(); // the clicker should not have to wait for the periodic refresh
        return true;
      }

      int unitIndex = findUnitIndex(info.getSlot());
      if (unitIndex == -1) return false;
      if (playNoPermissionsEffect(info.getPlayer(), ChallengesPermissions.COMMAND_TIMER_COUNTDOWN)) return true;

      getTimer().addCountdownSeconds(getChangeAmount(TIME_UNITS.get(unitIndex), info.isLeftClick(), info.isShiftClick()));
      SoundSample.PLOP.play(info.getPlayer());
      updatePages(); // the clicker should not have to wait for the periodic refresh
      return true;
    }

  }

}
