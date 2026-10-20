package net.cyperia.commons.common.logging.internal;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.LogLevel;
import net.cyperia.commons.common.logging.lib.Slf4jILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class Slf4jLoggerWrapper implements Slf4jILogger {

  protected final Logger logger;

  public Slf4jLoggerWrapper(@NotNull Logger logger) {
    this.logger = logger;
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void trace(String msg) {
    logger.trace(msg);
  }

  @Override
  public void trace(String format, Object arg) {
    logger.trace(format, arg);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    logger.trace(format, arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    logger.trace(format, arguments);
  }

  @Override
  public void trace(String msg, Throwable t) {
    logger.trace(msg, t);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return logger.isTraceEnabled(marker);
  }

  @Override
  public void trace(Marker marker, String msg) {
    logger.trace(marker, msg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    logger.trace(marker, format, arg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    logger.trace(marker, format, arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    logger.trace(marker, format, argArray);
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    logger.trace(marker, msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public void debug(String msg) {
    logger.debug(msg);
  }

  @Override
  public void debug(String format, Object arg) {
    logger.debug(format, arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    logger.debug(format, arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    logger.debug(format, arguments);
  }

  @Override
  public void debug(String msg, Throwable t) {
    logger.debug(msg, t);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return logger.isDebugEnabled(marker);
  }

  @Override
  public void debug(Marker marker, String msg) {
    logger.debug(marker, msg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    logger.debug(marker, format, arg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    logger.debug(marker, format, arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    logger.debug(marker, format, arguments);
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    logger.debug(marker, msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public void info(String msg) {
    logger.info(msg);
  }

  @Override
  public void info(String format, Object arg) {
    logger.info(format, arg);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  @Override
  public void info(String msg, Throwable t) {
    logger.info(msg, t);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return logger.isInfoEnabled(marker);
  }

  @Override
  public void info(Marker marker, String msg) {
    logger.info(marker, msg);
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    logger.info(marker, format, arg);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    logger.info(marker, format, arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    logger.info(marker, format, arguments);
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    logger.info(marker, msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void warn(String msg) {
    logger.warn(msg);
  }

  @Override
  public void warn(String format, Object arg) {
    logger.warn(format, arg);
  }

  @Override
  public void warn(String format, Object... arguments) {
    logger.warn(format, arguments);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    logger.warn(format, arg1, arg2);
  }

  @Override
  public void warn(String msg, Throwable t) {
    logger.warn(msg, t);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return logger.isWarnEnabled(marker);
  }

  @Override
  public void warn(Marker marker, String msg) {
    logger.warn(marker, msg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    logger.warn(marker, format, arg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    logger.warn(marker, format, arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    logger.warn(marker, format, arguments);
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    logger.warn(marker, msg, t);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public void error(String msg) {
    logger.error(msg);
  }

  @Override
  public void error(String format, Object arg) {
    logger.error(format, arg);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    logger.error(format, arg1, arg2);
  }

  @Override
  public void log(@NotNull LogLevel level, @Nullable String message, @NotNull Object... args) {
    switch (level) {
      case TRACE:
        trace(message, args);
        return;
      case DEBUG:
      case EXTENDED:
        debug(message, args);
        return;
      case STATUS:
      case INFO:
        info(message, args);
        return;
      case WARN:
        warn(message, args);
        return;
      case ERROR:
        error(message, args);
    }
  }

  @Override
  public void error(String format, Object... arguments) {
    logger.error(format, arguments);
  }

  @Override
  public void error(String msg, Throwable t) {
    logger.error(msg, t);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return logger.isErrorEnabled(marker);
  }

  @Override
  public void error(Marker marker, String msg) {
    logger.error(marker, msg);
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    logger.error(marker, format, arg);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    logger.error(marker, format, arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    logger.error(marker, format, arguments);
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    logger.error(marker, msg, t);
  }

  @NotNull
  @Override
  public LogLevel getMinLevel() {
    if (logger.isTraceEnabled()) {
      return LogLevel.TRACE;
    } else if (logger.isDebugEnabled()) {
      return LogLevel.DEBUG;
    } else if (logger.isInfoEnabled()) {
      return LogLevel.INFO;
    } else if (logger.isWarnEnabled()) {
      return LogLevel.WARN;
    } else {
      return LogLevel.ERROR;
    }
  }

  @NotNull
  @Override
  public ILogger setMinLevel(@NotNull LogLevel level) {
    return this;
  }

}
