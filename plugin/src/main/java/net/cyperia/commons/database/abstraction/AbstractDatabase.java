package net.cyperia.commons.database.abstraction;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.DatabaseConfig;
import net.cyperia.commons.database.SQLColumn;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.exceptions.DatabaseAlreadyConnectedException;
import net.cyperia.commons.database.exceptions.DatabaseConnectionClosedException;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDatabase implements Database {

  protected final DatabaseConfig config;

  public AbstractDatabase(@NotNull DatabaseConfig config) {
    this.config = config;
  }

  @Override
  public boolean disconnectSafely() {
    if (!isConnected()) return true;
    try {
      disconnect();
      LOGGER.info("Successfully closed connection to database of type " + this.getClass().getSimpleName());
      return true;
    } catch (DatabaseException ex) {
      LOGGER.error("Could not disconnect from database (" + this.getClass().getSimpleName() + ")", ex);
      return false;
    }
  }

  @Override
  public void disconnect() throws DatabaseException {
    checkConnection();
    try {
      disconnect0();
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  protected abstract void disconnect0() throws Exception;

  @Override
  public boolean connectSafely() {
    try {
      connect();
      LOGGER.status("Successfully created connection to database of type " + this.getClass().getSimpleName());
      return true;
    } catch (DatabaseException ex) {
      LOGGER.error("Could not connect to database (" + this.getClass().getSimpleName() + ")", ex);
      return false;
    }
  }

  @Override
  public void connect() throws DatabaseException {
    if (isConnected()) throw new DatabaseAlreadyConnectedException();
    try {
      connect0();
    } catch (Exception ex) {
      if (ex instanceof DatabaseException) throw (DatabaseException) ex;
      throw new DatabaseException(ex);
    }
  }

  protected abstract void connect0() throws Exception;

  @Override
  public void createTableSafely(@NotNull String name, @NotNull SQLColumn... columns) {
    try {
      createTable(name, columns);
    } catch (DatabaseException ex) {
      LOGGER.error("Could not create table (" + this.getClass().getSimpleName() + ")", ex);
    }
  }

  @NotNull
  @Override
  public SpecificDatabase getSpecificDatabase(@NotNull String name) {
    return new DefaultSpecificDatabase(this, name);
  }

  @NotNull
  @Override
  public DatabaseConfig getConfig() {
    return config;
  }

  protected final void checkConnection() throws DatabaseConnectionClosedException {
    if (!isConnected())
      throw new DatabaseConnectionClosedException();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[connected=" + isConnected() + "]";
  }
}
