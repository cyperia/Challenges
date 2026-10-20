package net.cyperia.challenges.plugin.management.menu.generator.impl.timer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.IMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.VersionedContentContainer;
import net.cyperia.challenges.plugin.management.menu.generator.types.ITimerMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.search.SearchFindable;
import net.cyperia.challenges.plugin.management.scheduler.policy.ExtraWorldPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;


public class TimerMenuGenerator extends AbstractTimerMenuGenerator implements VersionedContentContainer.FromList, ITimerMenuGenerator {

  public static final int START_SLOT = 13;
  public static final int SHOW_SLOT = 20;
  public static final int MODE_SLOT = 24;
  public static final int COUNTDOWN_SLOT = 30;
  public static final int TIME_SLOT = 32;

  public static final Material TIME_MATERIAL = Material.CLOCK;
  public static final Material COUNTDOWN_MATERIAL = Material.MUSIC_DISC_CREATOR;

  /**
   * What the item at {@link #START_SLOT} shows, and what a click on it does. A countdown that is set
   * is what the round starts through, so the plain click starts *it* rather than the timer, and the
   * shift click is the way past it.
   */
  @Getter
  @RequiredArgsConstructor
  private enum StartState {

    RUNNING(Material.LIME_DYE, "menu.timer.state.started", "menu.timer.interact.pause"),
    COUNTDOWN_RUNNING(Material.ORANGE_DYE, "menu.timer.state.countdown", "menu.timer.countdown.interact.stop"),
    COUNTDOWN_READY(Material.RED_DYE, "menu.timer.state.paused", "menu.timer.countdown.interact.start"),
    PAUSED(Material.RED_DYE, "menu.timer.state.paused", "menu.timer.interact.resume");

    private final Material material;
    private final String nameKey;
    private final String interactionKey;
  }

  @NotNull
  private static StartState getStartState(@NotNull ChallengeTimer timer) {
    if (timer.isStarted()) return StartState.RUNNING;
    if (timer.isCountdownRunning()) return StartState.COUNTDOWN_RUNNING;
    if (timer.getCountdown() > 0) return StartState.COUNTDOWN_READY;
    return StartState.PAUSED;
  }

  @Getter
  private final TimerTimeMenuGenerator timeGenerator = new TimerTimeMenuGenerator();
  @Getter
  private final TimerCountdownMenuGenerator countdownGenerator = new TimerCountdownMenuGenerator();

  public TimerMenuGenerator() {
    timeGenerator.setSubMenu(true);
    countdownGenerator.setSubMenu(true);

    ChallengeAPI.registerScheduler(this, timeGenerator, countdownGenerator);
    Challenges.getInstance().getLoaderRegistry().subscribe(LanguageLoader.class, () -> {
      updatePages();
      timeGenerator.updatePages();
      countdownGenerator.updatePages();
    });
  }

  @Override
  public void setMenuType(@NotNull MenuType<?> menuType) {
    // the MenuType.TIMER instance is not initialized yet when the sub menus are created
    super.setMenuType(menuType);
    timeGenerator.setMenuType(menuType);
    countdownGenerator.setMenuType(menuType);

    SearchFindable.register(
      SearchFindable.create("menu.timer.time", TIME_MATERIAL, menuType, timeGenerator::openMenu),
      SearchFindable.create("menu.timer.countdown", COUNTDOWN_MATERIAL, menuType, countdownGenerator::openMenu));
  }

  @ScheduledTask(ticks = 20, timerPolicy = TimerPolicy.ALWAYS, worldPolicy = ExtraWorldPolicy.ALWAYS)
  @TimerTask(status = {TimerStatus.PAUSED, TimerStatus.RUNNING, TimerStatus.COUNTDOWN}, worldPolicy = ExtraWorldPolicy.ALWAYS)
  public void updateTimerInventory() {
    updatePages();
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(@NotNull Player player) {
    return new TimerMenuPosition();
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, @NotNull Locale locale) {
    ChallengeTimer timer = getTimer();

    inventory.setItem(START_SLOT, createStartItem(getStartState(timer), locale));
    inventory.setItem(SHOW_SLOT, createStateItem(new ItemStack(timer.isHidden() ? Material.BARRIER : Material.ENDER_EYE),
      timer.isHidden() ? "hidden" : "shown", timer.isHidden() ? "show" : "hide", locale));
    inventory.setItem(MODE_SLOT, createStateItem(new ItemBuilder.SkullBuilder().setBase64Texture(
        timer.isCountingUp() ? DefaultItems.SkullTextures.GREEN_ARROW_UP : DefaultItems.SkullTextures.RED_ARROW_DOWN).build(),
      timer.isCountingUp() ? "counting-up" : "counting-down", timer.isCountingUp() ? "count-down" : "count-up", locale));

    inventory.setItem(COUNTDOWN_SLOT, createSubMenuItem(COUNTDOWN_MATERIAL, "countdown", countdownGenerator, locale));
    inventory.setItem(TIME_SLOT, createSubMenuItem(TIME_MATERIAL, "time", timeGenerator, locale));
  }

  @NotNull
  private ItemStack createStateItem(@NotNull ItemStack preset, @NotNull String state, @NotNull String interaction, @NotNull Locale locale) {
    return new ItemFormatter(preset)
      .name(MessageKey.of("menu.timer.state." + state))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.timer.interact." + interaction))
      .build(locale);
  }

  @NotNull
  private ItemStack createStartItem(@NotNull StartState state, @NotNull Locale locale) {
    return new ItemFormatter(state.getMaterial())
      .name(MessageKey.of(state.getNameKey()))
      .tooltip(MenuClickType.LEFT, MessageKey.of(state.getInteractionKey()))
      .tooltipIf(state == StartState.COUNTDOWN_READY || state == StartState.COUNTDOWN_RUNNING,
        MenuClickType.LEFT_SHIFT, MessageKey.of("menu.timer.interact.resume-now"))
      .build(locale);
  }

  @NotNull
  private ItemStack createSubMenuItem(@NotNull Material material, @NotNull String menu, @NotNull IMenuGenerator generator, @NotNull Locale locale) {
    return new ItemFormatter(material)
      .name(MessageKey.of("menu.timer." + menu + ".name"))
      .description(MessageKey.of("menu.timer." + menu + ".desc"))
      .tooltip(MenuClickType.LEFT, Interaction.OPEN)
      .suffixIfNewUpdated(generator)
      .build(locale);
  }

  @NotNull
  @Override
  public List<?> getVersionedContent() {
    return List.of(timeGenerator, countdownGenerator);
  }

  @Override
  public void openCountdownMenu(@NotNull Player player) {
    countdownGenerator.openMenu(player);
  }

  @Override
  public void openTimeMenu(@NotNull Player player) {
    timeGenerator.openMenu(player);
  }

  public class TimerMenuPosition extends SinglePageGeneratorMenuPosition {

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      return switch (info.getSlot()) {
        case TIME_SLOT -> openSubMenu(timeGenerator, info.getPlayer());
        case COUNTDOWN_SLOT -> openSubMenu(countdownGenerator, info.getPlayer());
        case START_SLOT -> handleStateClick(info, timer -> handleStartClick(timer, info.isShiftClick()));
        case MODE_SLOT -> handleStateClick(info, timer -> timer.setCountingUp(!timer.isCountingUp()));
        case SHOW_SLOT -> handleStateClick(info, timer -> timer.setHidden(!timer.isHidden()));
        default -> false;
      };
    }

    private void handleStartClick(@NotNull ChallengeTimer timer, boolean shiftClick) {
      switch (getStartState(timer)) {
        case RUNNING -> timer.pause(true);
        case COUNTDOWN_RUNNING -> {
          if (shiftClick) timer.resume();
          else timer.pauseCountdown();
        }
        case COUNTDOWN_READY -> {
          if (shiftClick) timer.resume();
          else timer.resumeCountdown();
        }
        case PAUSED -> timer.resume();
      }
    }

    private boolean openSubMenu(@NotNull AbstractTimerMenuGenerator menu, @NotNull Player player) {
      SoundSample.CLICK.play(player);
      menu.openMenu(player);
      return true;
    }

    private boolean handleStateClick(@NotNull MenuClickInfo info, @NotNull Consumer<ChallengeTimer> action) {
      if (playNoPermissionsEffect(info.getPlayer())) return true;
      action.accept(getTimer());
      updatePages(); // the clicker should not have to wait for the periodic refresh
      return true;
    }

  }

}
