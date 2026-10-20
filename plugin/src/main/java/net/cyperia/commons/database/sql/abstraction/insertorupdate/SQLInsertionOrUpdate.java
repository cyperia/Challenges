package net.cyperia.commons.database.sql.abstraction.insertorupdate;

import net.cyperia.commons.database.action.DatabaseInsertionOrUpdate;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.sql.abstraction.AbstractSQLDatabase;
import net.cyperia.commons.database.sql.abstraction.update.SQLUpdate;
import net.cyperia.commons.database.sql.abstraction.where.SQLWhere;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SQLInsertionOrUpdate extends SQLUpdate implements DatabaseInsertionOrUpdate {

  public SQLInsertionOrUpdate(@NotNull AbstractSQLDatabase database, @NotNull String table) {
    super(database, table);
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate where(@NotNull String column, @Nullable Object value) {
    super.where(column, value);
    return this;
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate where(@NotNull String column, @Nullable Number value) {
    super.where(column, value);
    return this;
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate where(@NotNull String column, @Nullable String value, boolean ignoreCase) {
    super.where(column, value);
    return this;
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate where(@NotNull String column, @Nullable String value) {
    super.where(column, value);
    return this;
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate whereNot(@NotNull String column, @Nullable Object value) {
    super.whereNot(column, value);
    return this;
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate set(@NotNull String column, @Nullable Object value) {
    super.set(column, value);
    return this;
  }

  @Override
  public Void execute() throws DatabaseException {
    if (database.query(table, where).execute().isSet()) {
      return super.execute();
    } else {
      Map<String, Object> insert = new HashMap<>(values);
      for (Entry<String, SQLWhere> entry : where.entrySet()) {
        Object[] args = entry.getValue().getArgs();
        if (args.length == 0) continue;
        insert.put(entry.getKey(), args[0]);
      }

      database.insert(table, insert).execute();
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

}
