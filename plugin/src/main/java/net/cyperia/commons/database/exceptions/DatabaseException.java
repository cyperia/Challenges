package net.cyperia.commons.database.exceptions;

import net.cyperia.commons.database.action.DatabaseAction;
import org.jetbrains.annotations.NotNull;

/**
 * @see DatabaseAlreadyConnectedException
 * @see DatabaseConnectionClosedException
 * @see DatabaseUnsupportedFeatureException
 * @see DatabaseAction#execute()
 */
public class DatabaseException extends Exception {

  protected DatabaseException() {
    super();
  }

  public DatabaseException(@NotNull String message) {
    super(message);
  }

  public DatabaseException(@NotNull Throwable cause) {
    super(cause);
  }

  public DatabaseException(@NotNull String message, @NotNull Throwable cause) {
    super(message, cause);
  }
}
