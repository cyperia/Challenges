package net.cyperia.commons.database.sql.sqlite;

import net.cyperia.commons.common.misc.FileUtils;
import net.cyperia.commons.database.DatabaseConfig;
import net.cyperia.commons.database.action.DatabaseListTables;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.sql.abstraction.AbstractSQLDatabase;
import net.cyperia.commons.database.sql.sqlite.list.SQLiteListTables;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class SQLiteDatabase extends AbstractSQLDatabase {

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException ex) {
      LOGGER.error("Could not load sqlite driver");
    }
  }

  protected final File file;

  public SQLiteDatabase(@NotNull DatabaseConfig config) {
    super(config);
    file = new File(config.getFile());
  }

  @Override
  public void connect() throws DatabaseException {
    try {
      FileUtils.createFilesIfNecessary(file);
    } catch (IOException ex) {
      throw new DatabaseException(ex);
    }

    super.connect();
  }

  @Override
  protected String createUrl() {
    return "jdbc:sqlite:" + file;
  }

  @NotNull
  @Override
  public DatabaseListTables listTables() {
    return new SQLiteListTables(this);
  }

}
