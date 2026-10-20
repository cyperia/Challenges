package net.cyperia.commons.common.logging;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public enum LogLevel {

  TRACE(0, "TRACE", "trace", Level.FINEST, false),
  DEBUG(2, "DEBUG", "debug", Level.FINER, false),
  EXTENDED(5, "EXTENDED", "extended", Level.FINE, false),
  STATUS(7, "STATUS", "status", Level.CONFIG, false),
  INFO(10, "INFO", "info", Level.INFO, false),
  WARN(15, "WARN", "warn", Level.WARNING, true),
  ERROR(25, "ERROR", "error", Level.SEVERE, true);

  private final String uppercaseName, lowercaseName;
  private final Level javaLevel;
  private final int value;
  private final boolean highlighted;

  LogLevel(int value, @NotNull String uppercaseName, @NotNull String lowercaseName, @NotNull Level javaLevel, boolean highlighted) {
    this.uppercaseName = uppercaseName;
    this.lowercaseName = lowercaseName;
    this.javaLevel = javaLevel;
    this.value = value;
    this.highlighted = highlighted;
  }

  @NotNull
  public Level getJavaUtilLevel() {
    return javaLevel;
  }

  public boolean isShownAtLoggerLevel(@NotNull LogLevel loggerLevel) {
    return this.getValue() >= loggerLevel.getValue();
  }

  public int getValue() {
    return value;
  }

  @NotNull
  public String getLowerCaseName() {
    return lowercaseName;
  }

  @NotNull
  public String getUpperCaseName() {
    return uppercaseName;
  }

  public boolean isHighlighted() {
    return highlighted;
  }

  @NotNull
  public static LogLevel fromJavaLevel(@NotNull Level level) {
    for (LogLevel logLevel : values()) {
      if (logLevel.getJavaUtilLevel().intValue() == level.intValue())
        return logLevel;
    }
    return INFO;
  }

  @NotNull
  public static LogLevel fromValue(int value) {
    for (LogLevel level : values()) {
      if (level.getValue() == value)
        return level;
    }
    return INFO;
  }

  @NotNull
  public static LogLevel fromName(@NotNull String name) {
    for (LogLevel level : values()) {
      if (level.getUpperCaseName().equalsIgnoreCase(name))
        return level;
    }
    return INFO;
  }
}
