package net.cyperia.commons.common.logging;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILoggerFactory {

  @NotNull
  @CheckReturnValue
  ILogger forName(@Nullable String name);

  void setDefaultLevel(@NotNull LogLevel level);

}
