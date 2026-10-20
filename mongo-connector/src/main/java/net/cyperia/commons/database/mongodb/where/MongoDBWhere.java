package net.cyperia.commons.database.mongodb.where;

import com.mongodb.client.model.Collation;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MongoDBWhere {

  @NotNull
  Bson toBson();

  @Nullable
  Collation getCollation();

}
