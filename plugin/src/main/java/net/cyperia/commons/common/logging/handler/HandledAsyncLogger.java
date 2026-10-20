package net.cyperia.commons.common.logging.handler;

import net.cyperia.commons.common.collection.NamedThreadFactory;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HandledAsyncLogger extends HandledLogger {

  protected final Executor executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("AsyncLogTask"));

  public HandledAsyncLogger(@NotNull LogLevel initialLevel) {
    super(initialLevel);
  }

  @Override
  protected void log0(@NotNull LogEntry entry) {
    executor.execute(() -> logNow(entry));
  }

}
