package net.cyperia.commons.database.mongodb.query;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import net.cyperia.commons.common.misc.MongoUtils;
import net.cyperia.commons.database.Order;
import net.cyperia.commons.database.abstraction.DefaultExecutedQuery;
import net.cyperia.commons.database.action.DatabaseQuery;
import net.cyperia.commons.database.action.ExecutedQuery;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.mongodb.MongoDBDatabase;
import net.cyperia.commons.database.mongodb.where.MongoDBWhere;
import net.cyperia.commons.database.mongodb.where.ObjectWhere;
import net.cyperia.commons.database.mongodb.where.StringIgnoreCaseWhere;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MongoDBQuery implements DatabaseQuery {

  protected final MongoDBDatabase database;
  protected final String collection;
  protected final Map<String, MongoDBWhere> where;
  protected Order order;
  protected String orderBy;

  public MongoDBQuery(@NotNull MongoDBDatabase database, @NotNull String collection) {
    this.database = database;
    this.collection = collection;
    this.where = new HashMap<>();
  }

  public MongoDBQuery(@NotNull MongoDBDatabase database, @NotNull String collection, @NotNull Map<String, MongoDBWhere> where) {
    this.database = database;
    this.collection = collection;
    this.where = where;
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String field, @Nullable Object value) {
    where.put(field, new ObjectWhere(field, value, Filters::eq));
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String field, @Nullable Number value) {
    return where(field, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String field, @Nullable String value) {
    return where(field, (Object) value);
  }

  @NotNull
  @Override
  public DatabaseQuery where(@NotNull String field, @Nullable String value, boolean ignoreCase) {
    if (!ignoreCase) return where(field, value);
    if (value == null) throw new NullPointerException("Cannot use where ignore case with null value");
    where.put(field, new StringIgnoreCaseWhere(field, value));
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery whereNot(@NotNull String field, @Nullable Object value) {
    where.put(field, new ObjectWhere(field, value, Filters::ne));
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery orderBy(@NotNull String column, @NotNull Order order) {
    this.orderBy = column;
    this.order = order;
    return this;
  }

  @NotNull
  @Override
  public DatabaseQuery select(@NotNull String... selection) {
    return this;
  }

  @NotNull
  @Override
  public ExecutedQuery execute() throws DatabaseException {
    try {
      FindIterable<Document> iterable = database.getCollection(collection).find();
      MongoUtils.applyWhere(iterable, where);
      MongoUtils.applyOrder(iterable, orderBy, order);

      List<Document> documents = iterable.into(new ArrayList<>());
      return createExecutedQuery(documents);
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @NotNull
  private ExecutedQuery createExecutedQuery(@NotNull List<Document> documents) {
    List<net.cyperia.commons.common.config.Document> results = new ArrayList<>(documents.size());
    for (Document document : documents) {
      results.add(new MongoDBResult(document));
    }

    return new DefaultExecutedQuery(results);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MongoDBQuery that = (MongoDBQuery) o;
    return database.equals(that.database) && collection.equals(that.collection) && where.equals(that.where) && order == that.order && Objects.equals(orderBy, that.orderBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, collection, where, order, orderBy);
  }

}
