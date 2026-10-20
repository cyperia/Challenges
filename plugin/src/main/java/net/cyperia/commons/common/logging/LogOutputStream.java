package net.cyperia.commons.common.logging;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LogOutputStream extends ByteArrayOutputStream {

  private final ILogger logger;
  private final LogLevel level;

  public LogOutputStream(@NotNull ILogger logger, @NotNull LogLevel level) {
    this.logger = logger;
    this.level = level;
  }

  @Override
  public void flush() throws IOException {
    String input = this.toString(StandardCharsets.UTF_8.name());
    this.reset();

    if (input != null && !input.isEmpty() && !input.equals(System.lineSeparator())) {
      logger.log(level, input);
    }
  }

}
