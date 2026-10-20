package net.cyperia.commons.common.logging.handler;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class HandledLogger implements ILogger {

  protected final Collection<LogHandler> handlers = new CopyOnWriteArrayList<>();
  protected LogLevel level;

  public HandledLogger(@NotNull LogLevel initialLevel) {
    this.level = initialLevel;
  }

  @Override
  public void log(@NotNull LogLevel level, @Nullable String message, @NotNull Object... args) {
    if (!level.isShownAtLoggerLevel(this.level)) return;
    Throwable exception = null;
    for (Object arg : args) {
      if (arg instanceof Throwable)
        exception = (Throwable) arg;
    }
    log0(new LogEntry(Instant.now(), Thread.currentThread().getName(), ILogger.formatMessage(message, args), level, exception));
  }

  public void log(@NotNull LogEntry entry) {
    if (!entry.getLevel().isShownAtLoggerLevel(this.level)) return;
    log0(entry);
  }

  protected abstract void log0(@NotNull LogEntry entry);

  protected void logNow(@NotNull LogEntry entry) {
    for (LogHandler handler : handlers) {
      try {
        handler.handle(entry);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @NotNull
  public HandledLogger addHandler(@NotNull LogHandler... handler) {
    handlers.addAll(Arrays.asList(handler));
    return this;
  }

  @NotNull
  @Override
  public LogLevel getMinLevel() {
    return level;
  }

  @NotNull
  @Override
  public ILogger setMinLevel(@NotNull LogLevel level) {
    this.level = level;
    return this;
  }

}
