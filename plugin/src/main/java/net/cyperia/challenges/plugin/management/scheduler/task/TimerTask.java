package net.cyperia.challenges.plugin.management.scheduler.task;

import net.cyperia.challenges.plugin.management.scheduler.policy.ChallengeStatusPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.ExtraWorldPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.FreshnessPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.PlayerCountPolicy;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimerTask {

  @NotNull
  TimerStatus[] status();

  boolean async() default true;

  @NotNull
  ChallengeStatusPolicy challengePolicy() default ChallengeStatusPolicy.ENABLED;

  @NotNull
  PlayerCountPolicy playerPolicy() default PlayerCountPolicy.SOMEONE;

  @NotNull
  ExtraWorldPolicy worldPolicy() default ExtraWorldPolicy.NOT_USED;

  @NotNull
  FreshnessPolicy freshnessPolicy() default FreshnessPolicy.ALWAYS;

}
