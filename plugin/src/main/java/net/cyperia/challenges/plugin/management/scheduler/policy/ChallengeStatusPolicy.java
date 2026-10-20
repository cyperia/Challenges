package net.cyperia.challenges.plugin.management.scheduler.policy;

import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public enum ChallengeStatusPolicy implements IPolicy {

  ALWAYS(challenge -> true),
  DISABLED(challenge -> !challenge.isEnabled()),
  ENABLED(IChallenge::isEnabled);

  private final Predicate<IChallenge> check;

  ChallengeStatusPolicy(@NotNull Predicate<IChallenge> check) {
    this.check = check;
  }

  @Override
  public boolean check(@NotNull Object holder) {
    return check.test((IChallenge) holder);
  }

  @Override
  public boolean isApplicable(@NotNull Object holder) {
    return holder instanceof IChallenge;
  }

}
