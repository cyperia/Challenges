package net.cyperia.commons.common.logging.internal;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class FallbackLogger implements ILogger {

  protected final PrintStream stream = System.err;
  protected final String name;

  protected LogLevel level = LogLevel.INFO;

  public FallbackLogger(@Nullable String name) {
    this.name = name;
  }

  public FallbackLogger() {
    this(null);
  }

  @Override
  public void log(@NotNull LogLevel level, @Nullable String message, @NotNull Object... args) {
    if (!isLevelEnabled(level)) return;
    stream.println(getLogMessage(level, ILogger.formatMessage(message, args), name));
    for (Object arg : args) {
      if (!(arg instanceof Throwable)) continue;
      ((Throwable) arg).printStackTrace(stream);
    }
  }

  @NotNull
  @Override
  public FallbackLogger setMinLevel(@NotNull LogLevel level) {
    this.level = level;
    return this;
  }

  @NotNull
  @Override
  public LogLevel getMinLevel() {
    return level;
  }

  @NotNull
  public static String getLogMessage(@NotNull LogLevel level, @NotNull String message, @Nullable String name) {
    Thread thread = Thread.currentThread();
    String threadName = thread.getName();
    String time = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    return name == null ?
      String.format("[%s: %s/%s]: %s", time, threadName, level.getUpperCaseName(), message) :
      String.format("[%s: %s/%s] %s: %s", time, threadName, level.getUpperCaseName(), name, message);
  }

}
