package net.cyperia.commons.database.mongodb.count;

import net.cyperia.commons.database.action.DatabaseCountEntries;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.mongodb.MongoDBDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MongoDBCountEntries implements DatabaseCountEntries {

  protected final MongoDBDatabase database;
  protected final String table;

  public MongoDBCountEntries(@NotNull MongoDBDatabase database, @NotNull String table) {
    this.database = database;
    this.table = table;
  }

  @NotNull
  @Override
  public Long execute() throws DatabaseException {
    try {
      return database.getCollection(table).countDocuments();
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MongoDBCountEntries that = (MongoDBCountEntries) o;
    return Objects.equals(database, that.database) && Objects.equals(table, that.table);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, table);
  }
}
