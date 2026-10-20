package net.cyperia.commons.database.sql.abstraction.query;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.database.Order;
import net.cyperia.commons.database.abstraction.DefaultExecutedQuery;
import net.cyperia.commons.database.action.DatabaseQuery;
import net.cyperia.commons.database.action.ExecutedQuery;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.sql.abstraction.AbstractSQLDatabase;
import net.cyperia.commons.database.sql.abstraction.where.ObjectWhere;
import net.cyperia.commons.database.sql.abstraction.where.SQLWhere;
import net.cyperia.commons.database.sql.abstraction.where.StringIgnoreCaseWhere;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class SQLQuery implements DatabaseQuery {

  protected final AbstractSQLDatabase database;
  protected final String table;
  protected final Map<String, SQLWhere> where;
  protected String[] selection = {"*"};
  protected String orderBy;
  protected Order order;

  public SQLQuery(@NotNull AbstractSQLDatabase database, @NotNull String table) {
    this.database = database;
    this.table = table;
    this.where = new HashMap<>();
  }

  public SQLQuery(@NotNull AbstractSQLDatabase database, @NotNull String table, @NotNull Map<String, SQLWhere> where) {
    this.database = database;
    this.table = table;
    this.where = where;
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String column, @Nullable Object object) {
    where.put(column, new ObjectWhere(column, object, "="));
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String column, @Nullable Number value) {
    return where(column, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String column, @Nullable String value) {
    return where(column, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String column, @Nullable String value, boolean ignoreCase) {
    if (!ignoreCase) return where(column, value);
    if (value == null) throw new NullPointerException("Cannot use where ignore case with null value");
    where.put(column, new StringIgnoreCaseWhere(column, value));
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery whereNot(@NotNull String column, @Nullable Object object) {
    where.put(column, new ObjectWhere(column, object, "!="));
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery orderBy(@NotNull String column, @NotNull Order order) {
    this.orderBy = column;
    this.order = order;
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery select(@NotNull String... selection) {
    if (selection.length == 0) throw new IllegalArgumentException("Cannot select noting");
    this.selection = selection;
    return this;
  }

  @NotNull
  protected PreparedStatement prepare() throws SQLException, DatabaseException {
    StringBuilder command = new StringBuilder();
    List<Object> args = new LinkedList<>();

    command.append("SELECT ");
    for (int i = 0; i < selection.length; i++) {
      if (i > 0) command.append(", ");
      command.append(selection[i]);
    }
    command.append(" FROM ");
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

    if (orderBy != null) {
      command.append(" ORDER BY ");
      command.append(orderBy);
      if (order != null)
        command.append(" " + (order == Order.HIGHEST ? "DESC" : "ASC"));
      command.append(" ");
    }

    return database.prepare(command.toString(), args.toArray());
  }

  @NotNull
  @Override
  public ExecutedQuery execute() throws DatabaseException {
    try {
      PreparedStatement statement = prepare();
      ResultSet result = statement.executeQuery();
      return createExecutedQuery(result);
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @NotNull
  private ExecutedQuery createExecutedQuery(@NotNull ResultSet result) throws SQLException {
    List<Document> results = new ArrayList<>();
    ResultSetMetaData data = result.getMetaData();
    while (result.next()) {
      Map<String, Object> map = new HashMap<>();
      for (int i = 1; i <= data.getColumnCount(); i++) {
        Object value = result.getObject(i);
        map.put(data.getColumnLabel(i), value);
      }
      Document row = new SQLResult(map);
      results.add(row);
    }
    result.close();

    return new DefaultExecutedQuery(results);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SQLQuery sqlQuery = (SQLQuery) o;
    return database.equals(sqlQuery.database) && table.equals(sqlQuery.table) && where.equals(sqlQuery.where) && Arrays.equals(selection, sqlQuery.selection) && Objects.equals(orderBy, sqlQuery.orderBy) && order == sqlQuery.order;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(database, table, where, orderBy, order);
    result = 31 * result + Arrays.hashCode(selection);
    return result;
  }

}
