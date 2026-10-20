package net.cyperia.commons.common.logging.handler;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface LogHandler {

  DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

  void handle(@NotNull LogEntry entry) throws Exception;

}
