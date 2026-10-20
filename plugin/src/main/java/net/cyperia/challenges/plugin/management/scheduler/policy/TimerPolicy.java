package net.cyperia.challenges.plugin.management.scheduler.policy;

import net.cyperia.challenges.plugin.ChallengeAPI;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public enum TimerPolicy implements IPolicy {

  ALWAYS(() -> true),
  PAUSED(ChallengeAPI::isPaused),
  STARTED(ChallengeAPI::isStarted),
  COUNTDOWN(ChallengeAPI::isCountdown);

  private final BooleanSupplier check;

  TimerPolicy(@NotNull BooleanSupplier check) {
    this.check = check;
  }

  @Override
  public boolean check(@NotNull Object holder) {
    return check.getAsBoolean();
  }

}
