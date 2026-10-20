package net.cyperia.challenges.plugin.management.scheduler.policy;

import net.cyperia.challenges.plugin.ChallengeAPI;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public enum ExtraWorldPolicy implements IPolicy {

  ALWAYS(() -> true),
  USED(ChallengeAPI::isWorldInUse),
  NOT_USED(() -> !ChallengeAPI.isWorldInUse());

  private final BooleanSupplier check;

  ExtraWorldPolicy(@NotNull BooleanSupplier check) {
    this.check = check;
  }

  @Override
  public boolean check(@NotNull Object holder) {
    return check.getAsBoolean();
  }

}
