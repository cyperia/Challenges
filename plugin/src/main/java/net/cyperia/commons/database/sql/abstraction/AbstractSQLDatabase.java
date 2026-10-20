package net.cyperia.commons.database.sql.abstraction;

import net.cyperia.commons.database.DatabaseConfig;
import net.cyperia.commons.database.SQLColumn;
import net.cyperia.commons.database.abstraction.AbstractDatabase;
import net.cyperia.commons.database.action.*;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.sql.abstraction.count.SQLCountEntries;
import net.cyperia.commons.database.sql.abstraction.deletion.SQLDeletion;
import net.cyperia.commons.database.sql.abstraction.insertion.SQLInsertion;
import net.cyperia.commons.database.sql.abstraction.insertorupdate.SQLInsertionOrUpdate;
import net.cyperia.commons.database.sql.abstraction.query.SQLQuery;
import net.cyperia.commons.database.sql.abstraction.update.SQLUpdate;
import net.cyperia.commons.database.sql.abstraction.where.SQLWhere;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public abstract class AbstractSQLDatabase extends AbstractDatabase {

  protected Connection connection;

  public AbstractSQLDatabase(@NotNull DatabaseConfig config) {
    super(config);
  }

  @Override
  public void disconnect0() throws Exception {
    connection.close();
    connection = null;
  }

  @Override
  public void connect0() throws Exception {
    connection = DriverManager.getConnection(createUrl(), config.getUser(), config.getPassword());
  }

  protected abstract String createUrl();

  @Override
  public boolean isConnected() {
    try {
      if (connection == null) return false;
      connection.isClosed();
      return true;
    } catch (SQLException ex) {
      LOGGER.error("Could not check connection state: " + ex.getMessage());
      return false;
    }
  }

  @Override
  public void createTable(@NotNull String name, @NotNull SQLColumn... columns) throws DatabaseException {
    try {
      StringBuilder command = new StringBuilder();
      command.append("CREATE TABLE IF NOT EXISTS `");
      command.append(name);
      command.append("` (");
      {
        int index = 0;
        for (SQLColumn column : columns) {
          if (index > 0) command.append(", ");
          command.append(column);
          index++;
        }
      }
      command.append(")");

      PreparedStatement statement = prepare(command.toString());
      statement.execute();
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @NotNull
  @Override
  public DatabaseCountEntries countEntries(@NotNull String table) {
    return new SQLCountEntries(this, table);
  }

  @NotNull
  @Override
  public DatabaseQuery query(@NotNull String table) {
    return new SQLQuery(this, table);
  }

  @NotNull
  public DatabaseQuery query(@NotNull String table, @NotNull Map<String, SQLWhere> where) {
    return new SQLQuery(this, table, where);
  }

  @NotNull
  @Override
  public DatabaseUpdate update(@NotNull String table) {
    return new SQLUpdate(this, table);
  }

  @NotNull
  @Override
  public DatabaseInsertion insert(@NotNull String table) {
    return new SQLInsertion(this, table);
  }

  @NotNull
  public DatabaseInsertion insert(@NotNull String table, @NotNull Map<String, Object> values) {
    return new SQLInsertion(this, table, values);
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate insertOrUpdate(@NotNull String table) {
    return new SQLInsertionOrUpdate(this, table);
  }

  @NotNull
  @Override
  public DatabaseDeletion delete(@NotNull String table) {
    return new SQLDeletion(this, table);
  }

  @NotNull
  public PreparedStatement prepare(@NotNull CharSequence command, @NotNull Object... args) throws SQLException, DatabaseException {
    checkConnection();
    PreparedStatement statement = connection.prepareStatement(command.toString());
    SQLHelper.fillParams(statement, args);
    return statement;
  }

}
