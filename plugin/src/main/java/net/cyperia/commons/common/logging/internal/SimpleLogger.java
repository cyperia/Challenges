package net.cyperia.commons.common.logging.internal;

import net.cyperia.commons.common.logging.LogLevel;
import net.cyperia.commons.common.logging.lib.Slf4jILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;

public class SimpleLogger extends FallbackLogger implements Slf4jILogger {

  public SimpleLogger(@Nullable String name) {
    super(name);
  }

  public SimpleLogger() {
    super();
  }

  @Override
  public void log(@NotNull LogLevel level, @Nullable Object message, @NotNull Object... args) {
    super.log(level, message, args);
  }

  @Override
  public void error(@Nullable String message, @NotNull Object... args) {
    super.error(message, args);
  }

  @Override
  public void error(@Nullable Object message, @NotNull Object... args) {
    super.error(message, args);
  }

  @Override
  public void warn(@Nullable String message, @NotNull Object... args) {
    super.warn(message, args);
  }

  @Override
  public void warn(@Nullable Object message, @NotNull Object... args) {
    super.warn(message, args);
  }

  @Override
  public void info(@Nullable String message, @NotNull Object... args) {
    super.info(message, args);
  }

  @Override
  public void info(@Nullable Object message, @NotNull Object... args) {
    super.info(message, args);
  }

  @Override
  public void status(@Nullable String message, @NotNull Object... args) {
    super.status(message, args);
  }

  @Override
  public void status(@Nullable Object message, @NotNull Object... args) {
    super.status(message, args);
  }

  @Override
  public void debug(@Nullable String message, @NotNull Object... args) {
    super.debug(message, args);
  }

  @Override
  public void debug(@Nullable Object message, @NotNull Object... args) {
    super.debug(message, args);
  }

  @Override
  public void trace(@Nullable String message, @NotNull Object... args) {
    super.trace(message, args);
  }

  @Override
  public void trace(@Nullable Object message, @NotNull Object... args) {
    super.trace(message, args);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isTraceEnabled() {
    return isLevelEnabled(LogLevel.TRACE);
  }

  @Override
  public void trace(String msg) {
    trace(msg, new Object[0]);
  }

  @Override
  public void trace(String format, Object arg) {
    trace(format, new Object[]{arg});
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    trace(format, new Object[]{arg1, arg2});
  }

  @Override
  public void trace(String msg, Throwable t) {
    trace(msg, new Object[]{t});
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return isTraceEnabled();
  }

  @Override
  public void trace(Marker marker, String msg) {
    trace(msg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    trace(format, arg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    trace(format, arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    trace(format, argArray);
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    trace(msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return isLevelEnabled(LogLevel.DEBUG);
  }

  @Override
  public void debug(String msg) {
    debug(msg, new Object[0]);
  }

  @Override
  public void debug(String format, Object arg) {
    debug(format, new Object[]{arg});
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    debug(format, new Object[]{arg1, arg2});
  }

  @Override
  public void debug(String msg, Throwable t) {
    debug(msg, new Object[]{t});
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  @Override
  public void debug(Marker marker, String msg) {
    debug(msg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    debug(format, arg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    debug(format, arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    debug(format, arguments);
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    debug(msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return isLevelEnabled(LogLevel.INFO);
  }

  @Override
  public void info(String msg) {
    info(msg, new Object[0]);
  }

  @Override
  public void info(String format, Object arg) {
    info(format, new Object[]{arg});
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    info(format, new Object[]{arg1, arg2});
  }

  @Override
  public void info(String msg, Throwable t) {
    info(msg, new Object[]{t});
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  @Override
  public void info(Marker marker, String msg) {
    info(msg);
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    info(format, arg);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    info(format, arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    info(format, arguments);
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    info(msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return isLevelEnabled(LogLevel.WARN);
  }

  @Override
  public void warn(String msg) {
    warn(msg, new Object[0]);
  }

  @Override
  public void warn(String format, Object arg) {
    warn(format, new Object[]{arg});
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    warn(format, new Object[]{arg1, arg2});
  }

  @Override
  public void warn(String msg, Throwable t) {
    warn(msg, new Object[]{t});
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  @Override
  public void warn(Marker marker, String msg) {
    warn(marker);
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    warn(format, arg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    warn(format, arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    warn(format, arguments);
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    warn(msg, t);
  }

  @Override
  public boolean isErrorEnabled() {
    return isLevelEnabled(LogLevel.ERROR);
  }

  @Override
  public void error(String msg) {
    error(msg, new Object[0]);
  }

  @Override
  public void error(String format, Object arg) {
    error(format, new Object[]{arg});
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    error(format, new Object[]{arg1, arg2});
  }

  @Override
  public void error(String msg, Throwable t) {
    error(msg, new Object[]{t});
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  @Override
  public void error(Marker marker, String msg) {
    error(msg);
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    error(format, arg);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    error(format, arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    error(format, arguments);
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    error(msg, t);
  }

}
