package net.cyperia.commons.common.logging.internal;

import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.LogLevel;
import net.cyperia.commons.common.logging.lib.JavaILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.*;

public class JavaLoggerWrapper extends JavaILogger {

  protected final Logger logger;

  public JavaLoggerWrapper(@NotNull Logger logger) {
    super(null, null);
    this.logger = logger;
  }

  @Override
  public boolean getUseParentHandlers() {
    return logger.getUseParentHandlers();
  }

  @Override
  public Filter getFilter() {
    return logger.getFilter();
  }

  @Override
  public Handler[] getHandlers() {
    return logger.getHandlers();
  }

  @Override
  public Level getLevel() {
    return logger.getLevel();
  }

  @Override
  public Logger getParent() {
    return logger.getParent();
  }

  @Override
  public ResourceBundle getResourceBundle() {
    return logger.getResourceBundle();
  }

  @Override
  public String getResourceBundleName() {
    return logger.getResourceBundleName();
  }

  @Override
  public void log(LogRecord record) {
    mapLevel(record);
    logger.log(record);
  }

  @Override
  public void log(Level level, String msg, Object[] params) {
    logger.log(mapLevel(level), msg, params);
  }

  @Override
  public void log(Level level, String msg, Object param1) {
    logger.log(mapLevel(level), msg, param1);
  }

  @Override
  public void log(Level level, Supplier<String> msgSupplier) {
    logger.log(mapLevel(level), msgSupplier);
  }

  @Override
  public void log(Level level, String msg) {
    logger.log(mapLevel(level), msg);
  }

  @Override
  public void log(Level level, String msg, Throwable thrown) {
    logger.log(mapLevel(level), msg, thrown);
  }

  @Override
  public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
    logger.log(mapLevel(level), thrown, msgSupplier);
  }

  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
    logger.logp(mapLevel(level), sourceClass, sourceMethod, msg);
  }

  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
    logger.logp(mapLevel(level), sourceClass, sourceMethod, msgSupplier);
  }

  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
    logger.logp(mapLevel(level), sourceClass, sourceMethod, msg, param1);
  }

  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
    logger.logp(mapLevel(level), sourceClass, sourceMethod, msg, params);
  }

  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
    logger.logp(mapLevel(level), sourceClass, sourceMethod, msg, thrown);
  }

  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
    logger.logp(mapLevel(level), sourceClass, sourceMethod, thrown, msgSupplier);
  }

  @Override
  public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Object... params) {
    logger.logrb(mapLevel(level), sourceClass, sourceMethod, bundle, msg, params);
  }

  @Override
  public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
    logger.logrb(mapLevel(level), sourceClass, sourceMethod, bundle, msg, thrown);
  }

  @Override
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
    logger.logrb(mapLevel(level), sourceClass, sourceMethod, bundleName, msg);
  }

  @Override
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object param1) {
    logger.logrb(mapLevel(level), sourceClass, sourceMethod, bundleName, msg, param1);
  }

  @Override
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object[] params) {
    logger.logrb(mapLevel(level), sourceClass, sourceMethod, bundleName, msg, params);
  }

  @Override
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Throwable thrown) {
    logger.logrb(mapLevel(level), sourceClass, sourceMethod, bundleName, msg, thrown);
  }

  @Override
  public boolean isLoggable(Level level) {
    return logger.isLoggable(level);
  }

  @Override
  public void setLevel(Level newLevel) throws SecurityException {
    logger.setLevel(newLevel);
  }

  @Override
  public void setFilter(Filter newFilter) throws SecurityException {
    logger.setFilter(newFilter);
  }

  @Override
  public void setParent(Logger parent) {
    logger.setParent(parent);
  }

  @Override
  public void setResourceBundle(ResourceBundle bundle) {
    logger.setResourceBundle(bundle);
  }

  @Override
  public void setUseParentHandlers(boolean useParentHandlers) {
    logger.setUseParentHandlers(useParentHandlers);
  }

  @Override
  public void severe(String msg) {
    logger.severe(msg);
  }

  @Override
  public void severe(Supplier<String> msgSupplier) {
    logger.severe(msgSupplier);
  }

  @Override
  public void entering(String sourceClass, String sourceMethod) {
    logger.entering(sourceClass, sourceMethod);
  }

  @Override
  public void entering(String sourceClass, String sourceMethod, Object param1) {
    logger.entering(sourceClass, sourceMethod, param1);
  }

  @Override
  public void entering(String sourceClass, String sourceMethod, Object[] params) {
    logger.entering(sourceClass, sourceMethod, params);
  }

  @Override
  public void exiting(String sourceClass, String sourceMethod) {
    logger.exiting(sourceClass, sourceMethod);
  }

  @Override
  public void exiting(String sourceClass, String sourceMethod, Object result) {
    logger.exiting(sourceClass, sourceMethod, result);
  }

  @Override
  public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
    logger.throwing(sourceClass, sourceMethod, thrown);
  }

  @Override
  public void warning(String msg) {
    logger.warning(msg);
  }

  @Override
  public void info(String msg) {
    logger.info(msg);
  }

  @Override
  public void config(String msg) {
    logger.config(msg);
  }

  @Override
  public void fine(String msg) {
    logger.fine(msg);
  }

  @Override
  public void finer(String msg) {
    logger.finer(msg);
  }

  @Override
  public void finest(String msg) {
    logger.finest(msg);
  }

  @Override
  public void warning(Supplier<String> msgSupplier) {
    logger.warning(msgSupplier);
  }

  @Override
  public void info(Supplier<String> msgSupplier) {
    logger.info(msgSupplier);
  }

  @Override
  public void config(Supplier<String> msgSupplier) {
    logger.config(msgSupplier);
  }

  @Override
  public void fine(Supplier<String> msgSupplier) {
    logger.fine(msgSupplier);
  }

  @Override
  public void finer(Supplier<String> msgSupplier) {
    logger.finer(msgSupplier);
  }

  @Override
  public void finest(Supplier<String> msgSupplier) {
    logger.finest(msgSupplier);
  }

  @Override
  public void addHandler(Handler handler) throws SecurityException {
    logger.addHandler(handler);
  }

  @Override
  public void removeHandler(Handler handler) throws SecurityException {
    logger.removeHandler(handler);
  }

  @Override
  public int hashCode() {
    return logger.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return logger.equals(obj);
  }

  @Override
  public String toString() {
    return logger.toString();
  }

  protected void mapLevel(@NotNull LogRecord record) {
    record.setLevel(mapLevel(record.getLevel()));
  }

  @NotNull
  protected Level mapLevel(@NotNull Level level) {
    return level;
  }

  @NotNull
  @Override
  public JavaILogger setMinLevel(@NotNull LogLevel level) {
    setLevel(level.getJavaUtilLevel());
    return this;
  }

  @NotNull
  @Override
  public LogLevel getMinLevel() {
    return LogLevel.fromJavaLevel(logger.getLevel());
  }

  @Override
  public void log(@NotNull LogLevel level, @Nullable String message, @NotNull Object... args) {
    Throwable thrown = null;
    for (Object arg : args) {
      if (arg instanceof Throwable)
        thrown = (Throwable) arg;
    }
    log(level.getJavaUtilLevel(), ILogger.formatMessage(message, args), thrown);
  }

  @Override
  public void log(@NotNull LogLevel level, @Nullable Object message, @NotNull Object... args) {
    log(level, String.valueOf(message), args);
  }

  @Override
  public void error(@Nullable String message, @NotNull Object... args) {
    log(LogLevel.ERROR, message, args);
  }

  @Override
  public void error(@Nullable Object message, @NotNull Object... args) {
    log(LogLevel.ERROR, message, args);
  }

  @Override
  public void warn(@Nullable String message, @NotNull Object... args) {
    log(LogLevel.WARN, message, args);
  }

  @Override
  public void warn(@Nullable Object message, @NotNull Object... args) {
    log(LogLevel.WARN, message, args);
  }

  @Override
  public void info(@Nullable String message, @NotNull Object... args) {
    log(LogLevel.INFO, message, args);
  }

  @Override
  public void info(@Nullable Object message, @NotNull Object... args) {
    log(LogLevel.INFO, message, args);
  }

  @Override
  public void status(@Nullable String message, @NotNull Object... args) {
    log(LogLevel.STATUS, message, args);
  }

  @Override
  public void status(@Nullable Object message, @NotNull Object... args) {
    log(LogLevel.STATUS, message, args);
  }

  @Override
  public void debug(@Nullable String message, @NotNull Object... args) {
    log(LogLevel.DEBUG, message, args);
  }

  @Override
  public void debug(@Nullable Object message, @NotNull Object... args) {
    log(LogLevel.DEBUG, message, args);
  }


  @Override
  public boolean isTraceEnabled() {
    return isLevelEnabled(LogLevel.TRACE);
  }

//	@Override
//	public void trace(String msg) {
//		trace(msg, new Object[0]);
//	}
//
//	@Override
//	public void trace(String format, Object arg) {
//		trace(format, new Object[] { arg });
//	}
//
//	@Override
//	public void trace(String format, Object arg1, Object arg2) {
//		trace(format, new Object[] { arg1, arg2 });
//	}
//
//	@Override
//	public void trace(String msg, Throwable t) {
//		trace(msg, new Object[] { t });
//	}
//
//	@Override
//	public boolean isTraceEnabled(Marker marker) {
//		return isTraceEnabled();
//	}
//
//	@Override
//	public void trace(Marker marker, String msg) {
//		trace(msg);
//	}
//
//	@Override
//	public void trace(Marker marker, String format, Object arg) {
//		trace(format, arg);
//	}
//
//	@Override
//	public void trace(Marker marker, String format, Object arg1, Object arg2) {
//		trace(format, arg1, arg2);
//	}
//
//	@Override
//	public void trace(Marker marker, String format, Object... argArray) {
//		trace(format, argArray);
//	}
//
//	@Override
//	public void trace(Marker marker, String msg, Throwable t) {
//		trace(msg, t);
//	}
//
//	@Override
//	public boolean isDebugEnabled() {
//		return isLevelEnabled(LogLevel.DEBUG);
//	}
//
//	@Override
//	public void debug(String msg) {
//		debug(msg, new Object[0]);
//	}
//
//	@Override
//	public void debug(String format, Object arg) {
//		debug(format, new Object[] { arg });
//	}
//
//	@Override
//	public void debug(String format, Object arg1, Object arg2) {
//		debug(format, new Object[] { arg1, arg2 });
//	}
//
//	@Override
//	public void debug(String msg, Throwable t) {
//		debug(msg, new Object[] { t });
//	}
//
//	@Override
//	public boolean isDebugEnabled(Marker marker) {
//		return isDebugEnabled();
//	}
//
//	@Override
//	public void debug(Marker marker, String msg) {
//		debug(msg);
//	}
//
//	@Override
//	public void debug(Marker marker, String format, Object arg) {
//		debug(format, arg);
//	}
//
//	@Override
//	public void debug(Marker marker, String format, Object arg1, Object arg2) {
//		debug(format, arg1, arg2);
//	}
//
//	@Override
//	public void debug(Marker marker, String format, Object... arguments) {
//		debug(format, arguments);
//	}
//
//	@Override
//	public void debug(Marker marker, String msg, Throwable t) {
//		debug(msg, t);
//	}
//
//	@Override
//	public boolean isInfoEnabled() {
//		return isLevelEnabled(LogLevel.INFO);
//	}
//
//	@Override
//	public void info(String format, Object arg) {
//		info(format, new Object[] { arg });
//	}
//
//	@Override
//	public void info(String format, Object arg1, Object arg2) {
//		info(format, new Object[] { arg1, arg2 });
//	}
//
//	@Override
//	public void info(String msg, Throwable t) {
//		info(msg, new Object[] { t });
//	}
//
//	@Override
//	public boolean isInfoEnabled(Marker marker) {
//		return isInfoEnabled();
//	}
//
//	@Override
//	public void info(Marker marker, String msg) {
//		info(msg);
//	}
//
//	@Override
//	public void info(Marker marker, String format, Object arg) {
//		info(format, arg);
//	}
//
//	@Override
//	public void info(Marker marker, String format, Object arg1, Object arg2) {
//		info(format, arg1, arg2);
//	}
//
//	@Override
//	public void info(Marker marker, String format, Object... arguments) {
//		info(format, arguments);
//	}
//
//	@Override
//	public void info(Marker marker, String msg, Throwable t) {
//		info(msg, t);
//	}
//
//	@Override
//	public boolean isWarnEnabled() {
//		return isLevelEnabled(LogLevel.WARN);
//	}
//
//	@Override
//	public void warn(String msg) {
//		warn(msg, new Object[0]);
//	}
//
//	@Override
//	public void warn(String format, Object arg) {
//		warn(format, new Object[] { arg });
//	}
//
//	@Override
//	public void warn(String format, Object arg1, Object arg2) {
//		warn(format, new Object[] { arg1, arg2 });
//	}
//
//	@Override
//	public void warn(String msg, Throwable t) {
//		warn(msg, new Object[] { t });
//	}
//
//	@Override
//	public boolean isWarnEnabled(Marker marker) {
//		return isWarnEnabled();
//	}
//
//	@Override
//	public void warn(Marker marker, String msg) {
//		warn(marker);
//	}
//
//	@Override
//	public void warn(Marker marker, String format, Object arg) {
//		warn(format, arg);
//	}
//
//	@Override
//	public void warn(Marker marker, String format, Object arg1, Object arg2) {
//		warn(format, arg1, arg2);
//	}
//
//	@Override
//	public void warn(Marker marker, String format, Object... arguments) {
//		warn(format, arguments);
//	}
//
//	@Override
//	public void warn(Marker marker, String msg, Throwable t) {
//		warn(msg, t);
//	}
//
//	@Override
//	public boolean isErrorEnabled() {
//		return isLevelEnabled(LogLevel.ERROR);
//	}
//
//	@Override
//	public void error(String msg) {
//		error(msg, new Object[0]);
//	}
//
//	@Override
//	public void error(String format, Object arg) {
//		error(format, new Object[] { arg });
//	}
//
//	@Override
//	public void error(String format, Object arg1, Object arg2) {
//		error(format, new Object[] { arg1, arg2 });
//	}
//
//	@Override
//	public void error(String msg, Throwable t) {
//		error(msg, new Object[] { t });
//	}
//
//	@Override
//	public boolean isErrorEnabled(Marker marker) {
//		return isErrorEnabled();
//	}
//
//	@Override
//	public void error(Marker marker, String msg) {
//		error(msg);
//	}
//
//	@Override
//	public void error(Marker marker, String format, Object arg) {
//		error(format, arg);
//	}
//
//	@Override
//	public void error(Marker marker, String format, Object arg1, Object arg2) {
//		error(format, arg1, arg2);
//	}
//
//	@Override
//	public void error(Marker marker, String format, Object... arguments) {
//		error(format, arguments);
//	}
//
//	@Override
//	public void error(Marker marker, String msg, Throwable t) {
//		error(msg, t);
//	}

}
