package net.cyperia.commons.database.mongodb.deletion;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import net.cyperia.commons.common.misc.BsonUtils;
import net.cyperia.commons.database.action.DatabaseDeletion;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.mongodb.MongoDBDatabase;
import net.cyperia.commons.database.mongodb.where.MongoDBWhere;
import net.cyperia.commons.database.mongodb.where.ObjectWhere;
import net.cyperia.commons.database.mongodb.where.StringIgnoreCaseWhere;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MongoDBDeletion implements DatabaseDeletion {

  protected final MongoDBDatabase database;
  protected final String collection;
  protected final Map<String, MongoDBWhere> where = new HashMap<>();

  public MongoDBDeletion(@NotNull MongoDBDatabase database, @NotNull String collection) {
    this.database = database;
    this.collection = collection;
  }

  @NotNull
  @Override
  public DatabaseDeletion where(@NotNull String column, @Nullable Object object) {
    where.put(column, new ObjectWhere(column, object, Filters::eq));
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
  public DatabaseDeletion whereNot(@NotNull String column, @Nullable Object object) {
    where.put(column, new ObjectWhere(column, object, Filters::ne));
    return this;
  }

  @Override
  public Void execute() throws DatabaseException {
    try {
      MongoCollection<Document> collection = database.getCollection(this.collection);

      Document filter = new Document();
      DeleteOptions options = new DeleteOptions();

      for (MongoDBWhere where : where.values()) {
        Bson whereBson = where.toBson();
        BsonDocument asBsonDocument = BsonUtils.convertBsonToBsonDocument(whereBson);
        filter.putAll(asBsonDocument);

        Collation collation = where.getCollation();
        if (collation != null)
          options.collation(collation);
      }

      collection.deleteMany(filter, options);
      return null;
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MongoDBDeletion that = (MongoDBDeletion) o;
    return database.equals(that.database) && collection.equals(that.collection) && where.equals(that.where);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, collection, where);
  }

}
