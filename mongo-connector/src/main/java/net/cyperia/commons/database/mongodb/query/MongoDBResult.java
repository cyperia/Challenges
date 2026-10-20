package net.cyperia.commons.database.mongodb.query;

import net.cyperia.commons.common.config.document.BsonDocument;
import org.jetbrains.annotations.NotNull;

public final class MongoDBResult extends BsonDocument {

  public MongoDBResult(@NotNull org.bson.Document bsonDocument) {
    super(bsonDocument);
  }

  @Override
  public boolean isReadonly() {
    return true;
  }

}
