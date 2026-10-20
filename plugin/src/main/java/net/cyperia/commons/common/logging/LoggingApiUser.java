package net.cyperia.commons.common.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LoggingApiUser {

  @NotNull
  ILogger getTargetLogger();

  default void error(@Nullable Object message, @NotNull Object... args) {
    getTargetLogger().error(message, args);
  }

  default void warn(@Nullable Object message, @NotNull Object... args) {
    getTargetLogger().warn(message, args);
  }

  default void info(@Nullable Object message, @NotNull Object... args) {
    getTargetLogger().info(message, args);
  }

  default void status(@Nullable Object message, @NotNull Object... args) {
    getTargetLogger().status(message, args);
  }

  default void extended(@Nullable Object message, @NotNull Object... args) {
    getTargetLogger().extended(message, args);
  }

  default void debug(@Nullable Object message, @NotNull Object... args) {
    getTargetLogger().debug(message, args);
  }

  default void trace(@Nullable Object message, @NotNull Object... args) {
    getTargetLogger().trace(message, args);
  }

  default boolean isTraceEnabled() {
    return getTargetLogger().isTraceEnabled();
  }

  default boolean isDebugEnabled() {
    return getTargetLogger().isDebugEnabled();
  }

}
