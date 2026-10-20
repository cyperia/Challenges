package net.cyperia.commons.common.config.exceptions;

import org.jetbrains.annotations.NotNull;

public final class ConfigReadOnlyException extends IllegalStateException {

  public ConfigReadOnlyException(@NotNull String action) {
    super("Config." + action);
  }

}
