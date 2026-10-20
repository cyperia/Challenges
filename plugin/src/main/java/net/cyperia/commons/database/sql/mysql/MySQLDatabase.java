package net.cyperia.commons.database.sql.mysql;

import net.cyperia.commons.database.DatabaseConfig;
import net.cyperia.commons.database.action.DatabaseListTables;
import net.cyperia.commons.database.sql.abstraction.AbstractSQLDatabase;
import net.cyperia.commons.database.sql.mysql.list.MySQLListTables;
import org.jetbrains.annotations.NotNull;

public class MySQLDatabase extends AbstractSQLDatabase {

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException ex) {
      LOGGER.error("Could not load mysql driver");
    }
  }

  public MySQLDatabase(@NotNull DatabaseConfig config) {
    super(config);
  }

  @NotNull
  @Override
  protected String createUrl() {
    return "jdbc:mysql://" + config.getHost() + (config.isPortSet() ? ":" + config.getPort() : "") + "/" + config.getDatabase();
  }

  @NotNull
  @Override
  public DatabaseListTables listTables() {
    return new MySQLListTables(this);
  }

}
