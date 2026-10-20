package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.challenges.type.SkippableTimer;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public abstract class TimedChallenge extends SettingModifier implements SkippableTimer {

  private final boolean runAsync;
  protected int secondsUntilActivation;
  private int originalSecondsUntilActivation;
  private boolean timerStatus = false;
  private boolean startedBefore = false;

  public TimedChallenge() {
    this(true);
  }

  public TimedChallenge(int max) {
    this(max, true);
  }

  public TimedChallenge(int min, int max) {
    this(min, max, true);
  }

  public TimedChallenge(int min, int max, int defaultValue) {
    this(min, max, defaultValue, true);
  }

  public TimedChallenge(boolean runAsync) {
    this.runAsync = runAsync;
  }

  public TimedChallenge(int max, boolean runAsync) {
    super(max);
    this.runAsync = runAsync;
  }

  public TimedChallenge(int min, int max, boolean runAsync) {
    super(min, max);
    this.runAsync = runAsync;
  }

  public TimedChallenge(int min, int max, int defaultValue, boolean runAsync) {
    super(min, max, defaultValue);
    this.runAsync = runAsync;
  }

  @Override
  public void setValue(int value) {
    super.setValue(value);
    if (!timerStatus) {
      restartTimer();
    }
  }

  @Override
  protected void onValueChange() {
    restartTimer();
  }

  // Don't execute async to prevent sync issues with timer
  @ScheduledTask(ticks = 20, async = false)
  public final void handleTimedChallengeSecond() {

    if (!startedBefore)
      restartTimer();

    if (timerStatus) {

      if (getTimerTrigger()) {
        secondsUntilActivation--;
        if (secondsUntilActivation <= 0) {
          secondsUntilActivation = 0;
          timerStatus = false;
          executeTimeActivation();
        } else {
          handleCountdown();
        }
      } else {
        Logger.debug("getTimerTrigger returned false for {}", this.getClass().getSimpleName());
      }
    }

  }

  public final void executeTimeActivation() {
    if (runAsync) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, this::onTimeActivation);
    } else {
      Bukkit.getScheduler().runTask(plugin, this::onTimeActivation);
    }
  }

  public final void shortCountDownTo(int seconds) {
    if (!timerStatus) throw new IllegalArgumentException("Countdown is not started");
    if (seconds > originalSecondsUntilActivation)
      throw new IllegalArgumentException("Cannot short countdown to a higher length than originally set");
    this.secondsUntilActivation = seconds;
  }

  public final boolean isTimerRunning() {
    return timerStatus;
  }

  @Override
  public boolean skipTimer() {
    if (secondsUntilActivation <= 0) return false;
    secondsUntilActivation = 0;
    return true;
  }

  public final int getSecondsLeftUntilNextActivation() {
    return secondsUntilActivation;
  }

  public final int getOriginalSecondsUntilActivation() {
    return originalSecondsUntilActivation;
  }

  protected float getProgress() {
    return getOriginalSecondsUntilActivation() == 0 ? 1 : (float) (getSecondsLeftUntilNextActivation()) / getOriginalSecondsUntilActivation();
  }

  protected void handleCountdown() {
  }

  protected boolean getTimerTrigger() {
    return true;
  }

  /**
   * @implSpec Derived from the {@link #getValueUnit() value unit}, which is what keeps the countdown and the
   * interval shown in the menu identical. Only override for a length that is not a function of the value.
   */
  protected int getSecondsUntilNextActivation() {
    ValueUnit unit = getValueUnit();
    if (!unit.isTime()) {
      throw new IllegalStateException(getClass().getSimpleName()
        + " must set a time ValueUnit or override getSecondsUntilNextActivation()");
    }
    return unit.roll(getValue(), getRandom());
  }

  /**
   * @implSpec Override to draw the jitter from a seeded random, so that the interval is reproducible.
   */
  @NotNull
  protected IRandom getRandom() {
    return globalRandom;
  }

  protected void restartTimer(int seconds) {
    Logger.debug("Restarting timer of {} with {} second(s)", this.getClass().getSimpleName(), seconds);

    startedBefore = true;
    secondsUntilActivation = seconds;
    originalSecondsUntilActivation = seconds;
    timerStatus = true;

    if (bossbar.isShown()) {
      bossbar.update();
    }
  }

  protected void restartTimer() {
    restartTimer(getSecondsUntilNextActivation());
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    if (document.isEmpty()) {
      startedBefore = true;
      timerStatus = true;
      restartTimer();
    } else if (document.contains("time")) {
      startedBefore = true;
      timerStatus = true;
      secondsUntilActivation = document.getInt("time");
      Logger.debug("Starting timer of {} from gamestate value with {} second(s)", this.getClass().getSimpleName(), secondsUntilActivation);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    if (secondsUntilActivation != originalSecondsUntilActivation) {
      document.set("time", secondsUntilActivation);
    }
  }

  protected abstract void onTimeActivation();

}
