package net.cyperia.challenges.plugin.management.scheduler.task;

import net.cyperia.challenges.plugin.management.scheduler.policy.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledTask {

  int ticks();

  boolean async() default true;

  @NotNull
  TimerPolicy timerPolicy() default TimerPolicy.STARTED;

  @NotNull
  ChallengeStatusPolicy challengePolicy() default ChallengeStatusPolicy.ENABLED;

  @NotNull
  PlayerCountPolicy playerPolicy() default PlayerCountPolicy.SOMEONE;

  @NotNull
  ExtraWorldPolicy worldPolicy() default ExtraWorldPolicy.NOT_USED;

  @NotNull
  FreshnessPolicy freshnessPolicy() default FreshnessPolicy.ALWAYS;

}
