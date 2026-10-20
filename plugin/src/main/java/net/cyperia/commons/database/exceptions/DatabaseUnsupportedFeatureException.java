package net.cyperia.commons.database.exceptions;

import org.jetbrains.annotations.NotNull;

public class DatabaseUnsupportedFeatureException extends DatabaseException {

  public DatabaseUnsupportedFeatureException() {
  }

  public DatabaseUnsupportedFeatureException(@NotNull String message) {
    super(message);
  }

}
