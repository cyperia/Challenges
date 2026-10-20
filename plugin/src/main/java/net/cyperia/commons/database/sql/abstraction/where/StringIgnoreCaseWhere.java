package net.cyperia.commons.database.sql.abstraction.where;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StringIgnoreCaseWhere implements SQLWhere {

  protected final String column;
  protected final String value;

  public StringIgnoreCaseWhere(@NotNull String column, @NotNull String value) {
    this.column = column;
    this.value = value;
  }

  @NotNull
  @Override
  public Object[] getArgs() {
    return new Object[]{value};
  }

  @NotNull
  @Override
  public String getAsSQLString() {
    return String.format("LOWER(%s) = LOWER(?)", column);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StringIgnoreCaseWhere that = (StringIgnoreCaseWhere) o;
    return column.equals(that.column) && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(column, value);
  }

}
