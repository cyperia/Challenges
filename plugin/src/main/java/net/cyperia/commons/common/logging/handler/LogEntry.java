package net.cyperia.commons.common.logging.handler;

import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class LogEntry {

  private Instant timestamp;
  private String threadName;
  private String message;
  private LogLevel level;
  private Throwable exception;

  public LogEntry(@NotNull Instant timestamp, @NotNull String threadName, @NotNull String message, @NotNull LogLevel level, @Nullable Throwable exception) {
    this.timestamp = timestamp;
    this.threadName = threadName;
    this.message = message;
    this.level = level;
    this.exception = exception;
  }

  @NotNull
  public Instant getTimestamp() {
    return timestamp;
  }

  @NotNull
  public String getThreadName() {
    return threadName;
  }

  @NotNull
  public String getMessage() {
    return message;
  }

  @NotNull
  public LogLevel getLevel() {
    return level;
  }

  @Nullable
  public Throwable getException() {
    return exception;
  }
}
