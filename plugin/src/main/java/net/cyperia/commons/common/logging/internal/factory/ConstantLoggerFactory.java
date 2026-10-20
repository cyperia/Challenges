package net.cyperia.commons.common.logging.internal.factory;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.ILoggerFactory;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConstantLoggerFactory implements ILoggerFactory {

  protected final ILogger logger;

  public ConstantLoggerFactory(@NotNull ILogger logger) {
    this.logger = logger;
  }

  @NotNull
  @Override
  public ILogger forName(@Nullable String name) {
    return logger;
  }

  @Override
  public void setDefaultLevel(@NotNull LogLevel level) {
    logger.setMinLevel(level);
  }

}
