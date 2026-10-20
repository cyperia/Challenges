package net.cyperia.commons.common.logging.lib;

import net.cyperia.commons.common.logging.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public interface Slf4jILogger extends ILogger, Logger {

  @Override
  boolean isTraceEnabled();

  @Override
  boolean isDebugEnabled();

  @Override
  boolean isInfoEnabled();

  @Override
  boolean isWarnEnabled();

  @Override
  boolean isErrorEnabled();

  @Override
  void trace(@Nullable String message, @NotNull Object... args);

  @Override
  void debug(@Nullable String message, @NotNull Object... args);

  @Override
  void info(@Nullable String message, @NotNull Object... args);

  @Override
  void warn(@Nullable String message, @NotNull Object... args);

  @Override
  void error(@Nullable String message, @NotNull Object... args);

}
