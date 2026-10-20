package net.cyperia.commons.database.exceptions;

/**
 * This exception is thrown, when a database operation is tried which requires a active connection, but the database is not connected.
 */
public class DatabaseConnectionClosedException extends DatabaseException {

  public DatabaseConnectionClosedException() {
    super("Database connection closed");
  }

}
