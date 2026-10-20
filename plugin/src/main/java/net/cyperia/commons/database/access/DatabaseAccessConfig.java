package net.cyperia.commons.database.access;

import org.jetbrains.annotations.NotNull;

public final class DatabaseAccessConfig {

  private final String table;
  private final String keyField;
  private final String valueField;

  public DatabaseAccessConfig(@NotNull String table, @NotNull String keyField, @NotNull String valueField) {
    this.table = table;
    this.keyField = keyField;
    this.valueField = valueField;
  }

  @NotNull
  public String getTable() {
    return table;
  }

  @NotNull
  public String getKeyField() {
    return keyField;
  }

  @NotNull
  public String getValueField() {
    return valueField;
  }

}
