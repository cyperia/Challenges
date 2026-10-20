package net.cyperia.commons.database;

import net.cyperia.commons.common.concurrent.task.Task;
import net.cyperia.commons.database.abstraction.DefaultExecutedQuery;
import net.cyperia.commons.database.abstraction.DefaultSpecificDatabase;
import net.cyperia.commons.database.action.*;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class EmptyDatabase implements Database {

  private final boolean silent;

  public EmptyDatabase(boolean silent) {
    this.silent = silent;
  }

  public boolean isSilent() {
    return silent;
  }

  protected void exception(@NotNull String message) {
    throw new UnsupportedOperationException(message);
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public void connect() throws DatabaseException {
    if (!silent)
      exception("Cannot connect with a NOP Database");
  }

  @Override
  public boolean connectSafely() {
    return false;
  }

  @Override
  public void disconnect() throws DatabaseException {
    if (!silent)
      exception("Cannot disconnect from a NOP Database");
  }

  @Override
  public boolean disconnectSafely() {
    return false;
  }

  @Override
  public void createTable(@NotNull String name, @NotNull SQLColumn... columns) throws DatabaseException {
    if (!silent)
      exception("Cannot create tables from a NOP Database");
  }

  @Override
  public void createTableSafely(@NotNull String name, @NotNull SQLColumn... columns) {
  }

  @NotNull
  @Override
  public DatabaseListTables listTables() {
    if (!silent)
      exception("Cannot list tables of a NOP Database");

    return new EmptyListTables();
  }

  @NotNull
  @Override
  public DatabaseCountEntries countEntries(@NotNull String table) {
    if (!silent)
      exception("Cannot count entries of a NOP Database");

    return new EmptyCountEntries();
  }

  @NotNull
  @Override
  public DatabaseQuery query(@NotNull String table) {
    if (!silent)
      exception("Cannot query in a NOP Database");

    return new EmptyDatabaseQuery();
  }

  @NotNull
  @Override
  public DatabaseUpdate update(@NotNull String table) {
    if (!silent)
      exception("Cannot update in a NOP Database");

    return new EmptyVoidAction();
  }

  @NotNull
  @Override
  public DatabaseInsertion insert(@NotNull String table) {
    if (!silent)
      exception("Cannot insert into a NOP Database");

    return new EmptyVoidAction();
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate insertOrUpdate(@NotNull String table) {
    if (!silent)
      exception("Cannot inset or update into a NOP Database");

    return new EmptyVoidAction();
  }

  @NotNull
  @Override
  public DatabaseDeletion delete(@NotNull String table) {
    if (!silent)
      exception("Cannot delete from a NOP Database");

    return new EmptyVoidAction();
  }

  @NotNull
  @Override
  public SpecificDatabase getSpecificDatabase(@NotNull String name) {
    return new DefaultSpecificDatabase(this, name);
  }

  @NotNull
  @Override
  public DatabaseConfig getConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "EmptyDatabase[silent=" + silent + "]";
  }

  public static class EmptyDatabaseQuery implements DatabaseQuery {

    @NotNull
    @Override
    public DatabaseQuery where(@NotNull String field, @Nullable Object object) {
      return this;
    }

    @NotNull
    @Override
    public DatabaseQuery where(@NotNull String field, @Nullable Number value) {
      return this;
    }

    @NotNull
    @Override
    public DatabaseQuery where(@NotNull String field, @Nullable String value, boolean ignoreCase) {
      return this;
    }

    @NotNull
    @Override
    public DatabaseQuery where(@NotNull String field, @Nullable String value) {
      return this;
    }

    @NotNull
    @Override
    public DatabaseQuery whereNot(@NotNull String field, @Nullable Object value) {
      return this;
    }

    @NotNull
    @Override
    public DatabaseQuery select(@NotNull String... selection) {
      return this;
    }

    @NotNull
    @Override
    public DatabaseQuery orderBy(@NotNull String field, @NotNull Order order) {
      return this;
    }

    @NotNull
    @Override
    public ExecutedQuery execute() throws DatabaseException {
      return new DefaultExecutedQuery(Collections.emptyList());
    }

    @NotNull
    @Override
    public Task<ExecutedQuery> executeAsync() {
      return Task.syncCall(this::execute);
    }

  }

  public static class EmptyVoidAction implements DatabaseDeletion, DatabaseInsertion, DatabaseUpdate, DatabaseInsertionOrUpdate {

    @NotNull
    @Override
    public EmptyVoidAction where(@NotNull String field, @Nullable Object value) {
      return this;
    }

    @NotNull
    @Override
    public EmptyVoidAction where(@NotNull String field, @Nullable Number value) {
      return this;
    }

    @NotNull
    @Override
    public EmptyVoidAction where(@NotNull String field, @Nullable String value, boolean ignoreCase) {
      return this;
    }

    @NotNull
    @Override
    public EmptyVoidAction where(@NotNull String field, @Nullable String value) {
      return this;
    }

    @NotNull
    @Override
    public EmptyVoidAction whereNot(@NotNull String field, @Nullable Object value) {
      return this;
    }

    @NotNull
    @Override
    public EmptyVoidAction set(@NotNull String field, @Nullable Object value) {
      return this;
    }

    @Override
    public Void execute() throws DatabaseException {
      return null;
    }

    @NotNull
    @Override
    public Task<Void> executeAsync() {
      return Task.completedVoid();
    }

  }

  public static class EmptyCountEntries implements DatabaseCountEntries {

    @NotNull
    @Override
    public Long execute() throws DatabaseException {
      return 0L;
    }

    @NotNull
    @Override
    public Task<Long> executeAsync() {
      return Task.completed(0L);
    }

  }

  public static class EmptyListTables implements DatabaseListTables {

    @NotNull
    @Override
    public List<String> execute() throws DatabaseException {
      return Collections.emptyList();
    }

    @NotNull
    @Override
    public Task<List<String>> executeAsync() {
      return Task.completed(Collections.emptyList());
    }

  }

}
