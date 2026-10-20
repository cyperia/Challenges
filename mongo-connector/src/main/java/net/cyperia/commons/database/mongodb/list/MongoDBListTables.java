package net.cyperia.commons.database.mongodb.list;

import net.cyperia.commons.database.action.DatabaseListTables;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.mongodb.MongoDBDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MongoDBListTables implements DatabaseListTables {

  protected final MongoDBDatabase database;

  public MongoDBListTables(@NotNull MongoDBDatabase database) {
    this.database = database;
  }

  @NotNull
  @Override
  public List<String> execute() throws DatabaseException {
    try {
      return database.getDatabase().listCollectionNames().into(new ArrayList<>());
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

}
