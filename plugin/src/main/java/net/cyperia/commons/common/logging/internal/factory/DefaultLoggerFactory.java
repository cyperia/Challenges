package net.cyperia.commons.common.logging.internal.factory;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.ILoggerFactory;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DefaultLoggerFactory implements ILoggerFactory {

  protected final Map<String, ILogger> loggers = new ConcurrentHashMap<>();
  protected final Function<? super String, ? extends ILogger> creator;
  protected LogLevel level = LogLevel.DEBUG;

  public DefaultLoggerFactory(@NotNull Function<? super String, ? extends ILogger> creator) {
    this.creator = creator;
  }

  @NotNull
  @Override
  @CheckReturnValue
  public synchronized ILogger forName(@Nullable String name) {
    return loggers.computeIfAbsent(name == null ? "anonymous" : name, unused -> creator.apply(name).setMinLevel(level));
  }

  @Override
  public void setDefaultLevel(@NotNull LogLevel level) {
    this.level = level;
  }

}
