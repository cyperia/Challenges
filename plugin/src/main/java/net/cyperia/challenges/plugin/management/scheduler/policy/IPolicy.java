package net.cyperia.challenges.plugin.management.scheduler.policy;

import org.jetbrains.annotations.NotNull;

public interface IPolicy {

  boolean check(@NotNull Object holder);

  default boolean isApplicable(@NotNull Object holder) {
    return true;
  }

}
