package net.cyperia.commons.common.logging.internal;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.WrappedILogger;
import net.cyperia.commons.common.logging.lib.Slf4jILogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MarkerIgnoringBase;

public class Slf4jILoggerWrapper extends MarkerIgnoringBase implements WrappedILogger, Slf4jILogger {

  private final ILogger logger;

  public Slf4jILoggerWrapper(@NotNull ILogger logger) {
    this.logger = logger;
  }

  @NotNull
  @Override
  public ILogger getWrappedLogger() {
    return logger;
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void trace(String message) {
    logger.trace(message);
  }

  @Override
  public void trace(String message, Object arg) {
    logger.trace(message, arg);
  }

  @Override
  public void trace(String message, Object arg1, Object arg2) {
    logger.trace(message, arg1, arg2);
  }

  @Override
  public void trace(String message, Object... args) {
    logger.trace(message, args);
  }

  @Override
  public void trace(String message, Throwable ex) {
    logger.trace(message, ex);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public void debug(String message) {
    logger.debug(message);
  }

  @Override
  public void debug(String message, Object arg) {
    logger.debug(message, arg);
  }

  @Override
  public void debug(String message, Object arg1, Object arg2) {
    logger.debug(message, arg1, arg2);
  }

  @Override
  public void debug(String message, Object... args) {
    logger.debug(message, args);
  }

  @Override
  public void debug(String message, Throwable ex) {
    logger.debug(message, ex);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public void info(String message) {
    logger.info(message);
  }

  @Override
  public void info(String message, Object arg) {
    logger.info(message, arg);
  }

  @Override
  public void info(String message, Object arg1, Object arg2) {
    logger.info(message, arg1, arg2);
  }

  @Override
  public void info(String message, Object... args) {
    logger.info(message, args);
  }

  @Override
  public void info(String message, Throwable ex) {
    logger.info(message, ex);
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void warn(String message) {
    logger.warn(message);
  }

  @Override
  public void warn(String message, Object arg) {
    logger.warn(message, arg);
  }

  @Override
  public void warn(String message, Object... args) {
    logger.warn(message, args);
  }

  @Override
  public void warn(String message, Object arg1, Object arg2) {
    logger.warn(message, arg1, arg2);
  }

  @Override
  public void warn(String message, Throwable ex) {
    logger.warn(message, ex);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void error(String message) {
    logger.error(message);
  }

  @Override
  public void error(String message, Object arg) {
    logger.error(message, arg);
  }

  @Override
  public void error(String message, Object arg1, Object arg2) {
    logger.error(message, arg1, arg2);
  }

  @Override
  public void error(String message, Object... args) {
    logger.error(message, args);
  }

  @Override
  public void error(String message, Throwable ex) {
    logger.error(message, ex);
  }
}
