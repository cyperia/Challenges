package net.cyperia.commons.bukkit.utils.logging;

import net.cyperia.commons.bukkit.core.BukkitModule;
import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.misc.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Logger {

  private Logger() {
  }

  @NotNull
  public static ILogger getInstance() {
    return BukkitModule.getProvidingModule(ReflectionUtils.getCaller()).getILogger();
  }

  public static void error(@Nullable Object message, @NotNull Object... args) {
    getInstance().error(message, args);
  }

  public static void warn(@Nullable Object message, @NotNull Object... args) {
    getInstance().warn(message, args);
  }

  public static void info(@Nullable Object message, @NotNull Object... args) {
    getInstance().info(message, args);
  }

  public static void debug(@Nullable Object message, @NotNull Object... args) {
    getInstance().debug(message, args);
  }

  public static void trace(@Nullable Object message, @NotNull Object... args) {
    getInstance().trace(message, args);
  }

}
