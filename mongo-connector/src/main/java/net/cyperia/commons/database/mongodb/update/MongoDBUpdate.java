package net.cyperia.commons.database.mongodb.update;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import net.cyperia.commons.common.misc.BsonUtils;
import net.cyperia.commons.common.misc.MongoUtils;
import net.cyperia.commons.database.action.DatabaseUpdate;
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
import java.util.Map.Entry;
import java.util.Objects;

public class MongoDBUpdate implements DatabaseUpdate {

  protected final MongoDBDatabase database;
  protected final String collection;
  protected final Map<String, MongoDBWhere> where;
  protected final Map<String, Object> values;

  public MongoDBUpdate(@NotNull MongoDBDatabase database, @NotNull String collection) {
    this.database = database;
    this.collection = collection;
    this.where = new HashMap<>();
    this.values = new HashMap<>();
  }

  public MongoDBUpdate(@NotNull MongoDBDatabase database, @NotNull String collection, @NotNull Map<String, MongoDBWhere> where, @NotNull Map<String, Object> values) {
    this.database = database;
    this.collection = collection;
    this.where = where;
    this.values = values;
  }

  @NotNull
  @Override
  public DatabaseUpdate where(@NotNull String field, @Nullable Object value) {
    where.put(field, new ObjectWhere(field, value, Filters::eq));
    return this;
  }

  @NotNull
  @Override
  public DatabaseUpdate where(@NotNull String field, @Nullable Number value) {
    return where(field, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseUpdate where(@NotNull String field, @Nullable String value) {
    return where(field, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseUpdate where(@NotNull String field, @Nullable String value, boolean ignoreCase) {
    if (!ignoreCase) return where(field, value);
    if (value == null) throw new NullPointerException("Cannot use where ignore case with null value");
    where.put(field, new StringIgnoreCaseWhere(field, value));
    return this;
  }

  @NotNull
  @Override
  public DatabaseUpdate whereNot(@NotNull String field, @Nullable Object value) {
    where.put(field, new ObjectWhere(field, value, Filters::ne));
    return this;
  }

  @NotNull
  @Override
  public DatabaseUpdate set(@NotNull String field, @Nullable Object value) {
    values.put(field, MongoUtils.packObject(value));
    return this;
  }

  @Override
  public Void execute() throws DatabaseException {
    try {
      MongoCollection<Document> collection = database.getCollection(this.collection);

      Document filter = new Document();
      UpdateOptions options = new UpdateOptions();

      for (MongoDBWhere where : where.values()) {
        Bson whereBson = where.toBson();
        BsonDocument asBsonDocument = BsonUtils.convertBsonToBsonDocument(whereBson);
        filter.putAll(asBsonDocument);

        Collation collation = where.getCollation();
        if (collation != null)
          options.collation(collation);
      }

      Document newDocument = new Document();
      for (Entry<String, Object> entry : values.entrySet()) {
        newDocument.put(entry.getKey(), MongoUtils.packObject(entry.getValue()));
      }

      BasicDBObject update = new BasicDBObject();
      update.put("$set", newDocument);

      collection.updateMany(filter, update, options);
      return null;
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MongoDBUpdate that = (MongoDBUpdate) o;
    return database.equals(that.database)
      && collection.equals(that.collection)
      && where.equals(that.where)
      && values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, collection, where, values);
  }

}
