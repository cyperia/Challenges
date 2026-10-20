package net.cyperia.commons.common.logging;

import net.cyperia.commons.common.logging.lib.JavaILogger;
import net.cyperia.commons.common.logging.lib.Slf4jILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;

public interface WrappedILogger extends ILogger {

  @NotNull
  ILogger getWrappedLogger();

  @Override
  default void log(@NotNull LogLevel level, @Nullable String message, @NotNull Object... args) {
    getWrappedLogger().log(level, message, args);
  }

  @Override
  default void log(@NotNull LogLevel level, @Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().log(level, message, args);
  }

  @Override
  default void error(@Nullable String message, @NotNull Object... args) {
    getWrappedLogger().error(message, args);
  }

  @Override
  default void error(@Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().error(message, args);
  }

  @Override
  default void warn(@Nullable String message, @NotNull Object... args) {
    getWrappedLogger().warn(message, args);
  }

  @Override
  default void warn(@Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().warn(message, args);
  }

  @Override
  default void info(@Nullable String message, @NotNull Object... args) {
    getWrappedLogger().info(message, args);
  }

  @Override
  default void info(@Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().info(message, args);
  }

  @Override
  default void status(@Nullable String message, @NotNull Object... args) {
    getWrappedLogger().status(message, args);
  }

  @Override
  default void status(@Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().status(message, args);
  }

  @Override
  default void extended(@Nullable String message, @NotNull Object... args) {
    getWrappedLogger().extended(message, args);
  }

  @Override
  default void extended(@Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().extended(message, args);
  }

  @Override
  default void debug(@Nullable String message, @NotNull Object... args) {
    getWrappedLogger().debug(message, args);
  }

  @Override
  default void debug(@Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().debug(message, args);
  }

  @Override
  default void trace(@Nullable String message, @NotNull Object... args) {
    getWrappedLogger().trace(message, args);
  }

  @Override
  default void trace(@Nullable Object message, @NotNull Object... args) {
    getWrappedLogger().trace(message, args);
  }

  @Override
  default boolean isLevelEnabled(@NotNull LogLevel level) {
    return getWrappedLogger().isLevelEnabled(level);
  }

  @Override
  default boolean isTraceEnabled() {
    return getWrappedLogger().isTraceEnabled();
  }

  @Override
  default boolean isDebugEnabled() {
    return getWrappedLogger().isDebugEnabled();
  }

  @Override
  default boolean isExtendedEnabled() {
    return getWrappedLogger().isExtendedEnabled();
  }

  @Override
  default boolean isInfoEnabled() {
    return getWrappedLogger().isInfoEnabled();
  }

  @Override
  default boolean isWarnEnabled() {
    return getWrappedLogger().isWarnEnabled();
  }

  @Override
  default boolean isErrorEnabled() {
    return getWrappedLogger().isErrorEnabled();
  }

  @NotNull
  @Override
  default LogLevel getMinLevel() {
    return getWrappedLogger().getMinLevel();
  }

  @NotNull
  @Override
  default ILogger setMinLevel(@NotNull LogLevel level) {
    return getWrappedLogger().setMinLevel(level);
  }

  @NotNull
  @Override
  default Slf4jILogger slf4j() {
    return getWrappedLogger().slf4j();
  }

  @NotNull
  @Override
  default JavaILogger java() {
    return getWrappedLogger().java();
  }

  @NotNull
  @Override
  default PrintStream asPrintStream(@NotNull LogLevel level) {
    return getWrappedLogger().asPrintStream(level);
  }
}
