package net.cyperia.commons.database.sql.abstraction.deletion;

import net.cyperia.commons.database.action.DatabaseDeletion;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.sql.abstraction.AbstractSQLDatabase;
import net.cyperia.commons.database.sql.abstraction.where.ObjectWhere;
import net.cyperia.commons.database.sql.abstraction.where.SQLWhere;
import net.cyperia.commons.database.sql.abstraction.where.StringIgnoreCaseWhere;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class SQLDeletion implements DatabaseDeletion {

  protected final AbstractSQLDatabase database;
  protected final String table;
  protected final Map<String, SQLWhere> where = new HashMap<>();

  public SQLDeletion(@NotNull AbstractSQLDatabase database, @NotNull String table) {
    this.database = database;
    this.table = table;
  }

  @NotNull
  @Override
  public DatabaseDeletion where(@NotNull String column, @Nullable Object value) {
    where.put(column, new ObjectWhere(column, value, "="));
    return this;
  }

  @NotNull
  @Override
  public DatabaseDeletion where(@NotNull String column, @Nullable Number value) {
    return where(column, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseDeletion where(@NotNull String column, @Nullable String value) {
    return where(column, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseDeletion where(@NotNull String column, @Nullable String value, boolean ignoreCase) {
    if (!ignoreCase) return where(column, value);
    if (value == null) throw new NullPointerException("Cannot use where ignore case with null value");
    where.put(column, new StringIgnoreCaseWhere(column, value));
    return this;
  }

  @NotNull
  @Override
  public DatabaseDeletion whereNot(@NotNull String column, @Nullable Object value) {
    where.put(column, new ObjectWhere(column, value, "!="));
    return this;
  }

  @NotNull
  protected PreparedStatement prepare() throws SQLException, DatabaseException {
    StringBuilder command = new StringBuilder();
    List<Object> args = new ArrayList<>();

    command.append("DELETE FROM ");
    command.append(table);

    if (!where.isEmpty()) {
      command.append(" WHERE ");
      int index = 0;
      for (Entry<String, SQLWhere> entry : where.entrySet()) {
        SQLWhere where = entry.getValue();
        if (index > 0) command.append(" AND ");
        command.append(where.getAsSQLString());
        args.addAll(Arrays.asList(where.getArgs()));
        index++;
      }
    }

    return database.prepare(command.toString(), args.toArray());
  }

  @Override
  public Void execute() throws DatabaseException {
    try {
      PreparedStatement statement = prepare();
      statement.execute();
      return null;
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SQLDeletion that = (SQLDeletion) o;
    return database.equals(that.database) && table.equals(that.table) && where.equals(that.where);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, table, where);
  }

}
