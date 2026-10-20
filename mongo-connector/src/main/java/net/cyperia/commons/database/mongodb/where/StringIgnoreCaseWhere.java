package net.cyperia.commons.database.mongodb.where;

import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringIgnoreCaseWhere implements MongoDBWhere {

  protected final String field;
  protected final String value;

  public StringIgnoreCaseWhere(@NotNull String field, @NotNull String value) {
    this.field = field;
    this.value = value;
  }

  @NotNull
  @Override
  public Bson toBson() {
    return Filters.eq(field, value);
  }

  @Nullable
  @Override
  public Collation getCollation() {
    return Collation.builder().collationStrength(CollationStrength.SECONDARY).locale("en").build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StringIgnoreCaseWhere that = (StringIgnoreCaseWhere) o;
    return field.equals(that.field) && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, value);
  }

}
