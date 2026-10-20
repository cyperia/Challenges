package net.cyperia.commons.common.logging.lib;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public abstract class JavaILogger extends Logger implements ILogger {

  protected JavaILogger(String name, String resourceBundleName) {
    super(name, resourceBundleName);
  }

  @NotNull
  @Override
  public abstract JavaILogger setMinLevel(@NotNull LogLevel level);

}
