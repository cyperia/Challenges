package net.cyperia.commons.common.logging.internal;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Because you can't change the level of a plugin logger properly, we create a wrapper for the plugin logger
 * and map all levels below {@link Level#INFO} to {@link Level#INFO}, if they are loggable.
 */
public class BukkitLoggerWrapper extends JavaLoggerWrapper {

  public BukkitLoggerWrapper(@NotNull Logger logger) {
    super(logger);
  }

  @NotNull
  @Override
  protected Level mapLevel(@NotNull Level level) {
    if (isLoggable(level) && level.intValue() < Level.INFO.intValue())
      return Level.INFO;
    return level;
  }

}
