package net.cyperia.commons.common.debug;

import net.cyperia.commons.common.collection.NumberFormatter;
import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.misc.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TimingsHelper {

  public static final ILogger LOGGER = ILogger.forThisClass();
  private static final Map<String, Long> timings = new ConcurrentHashMap<>();

  private TimingsHelper() {
  }

  public static void start(@NotNull String id) {
    timings.put(id, System.currentTimeMillis());
  }

  public static void stop(@NotNull String id) {
    Long start = timings.remove(id);
    if (start == null) {
      LOGGER.warn("Stopped timing {} which was not started before", id);
      return;
    }

    long time = System.currentTimeMillis() - start;
    LOGGER.debug("Finished timings '{}' within {}ms ({}s)", id, time, NumberFormatter.DOUBLE_FLOATING_POINT.format(time / 1000d));
  }

  public static void restart(@NotNull String id) {
    stop(id);
    start(id);
  }

  public static void start() {
    start(ReflectionUtils.getCallerName());
  }

  public static void stop() {
    stop(ReflectionUtils.getCallerName());
  }

  public static void restart() {
    restart(ReflectionUtils.getCallerName());
  }

}
