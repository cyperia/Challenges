package net.cyperia.commons.database.exceptions;

/**
 * This exception in thrown, when a database tries to connect but is already connected.
 */
public class DatabaseAlreadyConnectedException extends DatabaseException {

  public DatabaseAlreadyConnectedException() {
    super("Database already connected");
  }

}
