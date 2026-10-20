package net.cyperia.commons.database;

import net.cyperia.commons.common.concurrent.task.Task;
import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.database.action.*;
import net.cyperia.commons.database.exceptions.DatabaseAlreadyConnectedException;
import net.cyperia.commons.database.exceptions.DatabaseConnectionClosedException;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public interface Database {

  ILogger LOGGER = ILogger.forThisClass();

  @NotNull
  @CheckReturnValue
  static Database empty() {
    return new EmptyDatabase(true);
  }

  @NotNull
  @CheckReturnValue
  static Database unsupported() {
    return new EmptyDatabase(false);
  }

  boolean isConnected();

  /**
   * Creates the connection to the database synchronously.
   *
   * @throws DatabaseException                 If the connection could not be established
   * @throws DatabaseAlreadyConnectedException If this database is already {@link #isConnected() connected}
   */
  void connect() throws DatabaseException;

  /**
   * Creates the connection to the database synchronously.
   * No exceptions will be thrown if the process fails.
   *
   * @return {@code true} if the connection was established successfully
   */
  boolean connectSafely();

  /**
   * Closes the connection to the database synchronously.
   *
   * @throws DatabaseException                 If something went wrong while closing the connection to the database
   * @throws DatabaseConnectionClosedException If this database is not {@link #isConnected() connected}
   */
  void disconnect() throws DatabaseException;

  /**
   * Closes the connection to the database synchronously.
   * No exceptions will be thrown if the process fails.
   *
   * @return {@code true} if the connection was closed without errors
   */
  boolean disconnectSafely();

  void createTable(@NotNull String name, @NotNull SQLColumn... columns) throws DatabaseException;

  void createTableSafely(@NotNull String name, @NotNull SQLColumn... columns);

  @NotNull
  default Task<Void> createTableAsync(@NotNull String name, @NotNull SQLColumn... columns) {
    return Task.asyncRunExceptionally(() -> createTable(name, columns));
  }

  @NotNull
  @CheckReturnValue
  DatabaseListTables listTables();

  @NotNull
  @CheckReturnValue
  DatabaseCountEntries countEntries(@NotNull String table);

  @NotNull
  @CheckReturnValue
  DatabaseQuery query(@NotNull String table);

  @NotNull
  @CheckReturnValue
  DatabaseUpdate update(@NotNull String table);

  @NotNull
  @CheckReturnValue
  DatabaseInsertion insert(@NotNull String table);

  @NotNull
  @CheckReturnValue
  DatabaseInsertionOrUpdate insertOrUpdate(@NotNull String table);

  @NotNull
  @CheckReturnValue
  DatabaseDeletion delete(@NotNull String table);

  @NotNull
  @CheckReturnValue
  SpecificDatabase getSpecificDatabase(@NotNull String name);

  @NotNull
  DatabaseConfig getConfig();

}
