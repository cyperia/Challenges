package net.cyperia.commons.common.concurrent.task;

import org.jetbrains.annotations.NotNull;

public interface TaskListener<T> {

  default void onComplete(@NotNull Task<T> task, @NotNull T value) {
  }

  default void onCancelled(@NotNull Task<T> task) {
  }

  default void onFailure(@NotNull Task<T> task, @NotNull Throwable ex) {
  }

}
