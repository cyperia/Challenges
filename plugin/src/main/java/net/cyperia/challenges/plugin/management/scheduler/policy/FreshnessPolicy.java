package net.cyperia.challenges.plugin.management.scheduler.policy;

import net.cyperia.challenges.plugin.ChallengeAPI;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public enum FreshnessPolicy implements IPolicy {

  ALWAYS(() -> true),
  FRESH(ChallengeAPI::isFresh),
  NOT_FRESH(() -> !ChallengeAPI.isFresh());

  private final BooleanSupplier check;

  FreshnessPolicy(@NotNull BooleanSupplier check) {
    this.check = check;
  }

  @Override
  public boolean check(@NotNull Object holder) {
    return check.getAsBoolean();
  }

}
