package net.cyperia.commons.common.logging.internal.factory;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.ILoggerFactory;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

public class Slf4jLoggerFactory implements ILoggerFactory {

  @NotNull
  @Override
  public ILogger forName(@Nullable String name) {
    return ILogger.forSlf4jLogger(
      LoggerFactory.getLogger(name == null ? "Logger" : name)
    );
  }

  @Override
  public void setDefaultLevel(@NotNull LogLevel level) {
  }

}
