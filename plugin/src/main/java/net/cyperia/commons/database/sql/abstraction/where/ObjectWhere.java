package net.cyperia.commons.database.sql.abstraction.where;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ObjectWhere implements SQLWhere {

  protected final String column;
  protected final Object value;
  protected final String comparator;

  public ObjectWhere(@NotNull String column, @Nullable Object value, @NotNull String comparator) {
    this.column = column;
    this.value = value;
    this.comparator = comparator;
  }

  @NotNull
  @Override
  public Object[] getArgs() {
    return new Object[]{value};
  }

  @NotNull
  @Override
  public String getAsSQLString() {
    return String.format("`%s` %s ?", column, comparator);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ObjectWhere that = (ObjectWhere) o;
    return column.equals(that.column) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(column, value);
  }

}
