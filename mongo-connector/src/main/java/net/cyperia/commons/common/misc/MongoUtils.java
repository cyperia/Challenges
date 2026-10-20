package net.cyperia.commons.common.misc;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.Sorts;
import net.cyperia.commons.common.config.Json;
import net.cyperia.commons.database.Order;
import net.cyperia.commons.database.mongodb.where.MongoDBWhere;
import org.bson.BsonDocument;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public final class MongoUtils {

  private MongoUtils() {
  }

  public static void applyWhere(@NotNull FindIterable<Document> iterable, @NotNull Map<String, MongoDBWhere> where) {
    for (Entry<String, MongoDBWhere> entry : where.entrySet()) {
      MongoDBWhere value = entry.getValue();
      iterable.filter(value.toBson());

      Collation collation = value.getCollation();
      if (collation != null)
        iterable.collation(collation);
    }
  }

  public static void applyOrder(@NotNull FindIterable<Document> iterable, @Nullable String orderBy, @Nullable Order order) {
    if (order == null || orderBy == null) return;
    switch (order) {
      case HIGHEST:
        iterable.sort(Sorts.descending(orderBy));
        break;
      case LOWEST:
        iterable.sort(Sorts.ascending(orderBy));
        break;
    }
  }

  @Nullable
  public static Object packObject(@Nullable Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Json) {
      String json = ((Json) value).toJson();
      return Document.parse(json);
    } else if (BukkitReflectionSerializationUtils.isSerializable(value.getClass())) {
      Map<String, Object> values = BukkitReflectionSerializationUtils.serializeObject(value);
      if (values == null) return null;
      BsonDocument bson = new BsonDocument();
      BsonUtils.setDocumentProperties(bson, values);
      return bson;
    } else if (value instanceof UUID) {
      return ((UUID) value).toString();
    } else {
      return value;
    }
  }

}
