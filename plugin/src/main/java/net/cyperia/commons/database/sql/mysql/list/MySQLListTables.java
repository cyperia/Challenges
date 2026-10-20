package net.cyperia.commons.database.sql.mysql.list;

import net.cyperia.commons.database.action.DatabaseListTables;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.sql.abstraction.AbstractSQLDatabase;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MySQLListTables implements DatabaseListTables {

  protected final AbstractSQLDatabase database;

  public MySQLListTables(@NotNull AbstractSQLDatabase database) {
    this.database = database;
  }

  @NotNull
  @Override
  public List<String> execute() throws DatabaseException {
    try {
      PreparedStatement statement = database.prepare("SHOW TABLES");
      ResultSet result = statement.executeQuery();

      List<String> tables = new ArrayList<>();
      while (result.next()) {
        tables.add(result.getString(1));
      }
      return tables;
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

}
