package net.cyperia.commons.database.mongodb.insertion;

import net.cyperia.commons.common.misc.MongoUtils;
import net.cyperia.commons.database.action.DatabaseInsertion;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.mongodb.MongoDBDatabase;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MongoDBInsertion implements DatabaseInsertion {

  protected final MongoDBDatabase database;
  protected final String collection;
  protected final Document values;

  public MongoDBInsertion(@NotNull MongoDBDatabase database, @NotNull String collection) {
    this.database = database;
    this.collection = collection;
    this.values = new Document();
  }

  public MongoDBInsertion(@NotNull MongoDBDatabase database, @NotNull String collection, @NotNull Document values) {
    this.database = database;
    this.collection = collection;
    this.values = values;
  }

  @NotNull
  @Override
  public DatabaseInsertion set(@NotNull String field, @Nullable Object value) {
    values.put(field, MongoUtils.packObject(value));
    return this;
  }

  @Override
  public Void execute() throws DatabaseException {
    try {
      database.getCollection(collection).insertOne(values);
      return null;
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MongoDBInsertion that = (MongoDBInsertion) o;
    return database.equals(that.database) && collection.equals(that.collection) && values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, collection, values);
  }

}
