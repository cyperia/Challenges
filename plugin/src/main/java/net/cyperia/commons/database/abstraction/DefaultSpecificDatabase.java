package net.cyperia.commons.database.abstraction;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.action.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DefaultSpecificDatabase implements SpecificDatabase {

  protected final Database parent;
  protected final String name;

  public DefaultSpecificDatabase(@NotNull Database parent, @NotNull String name) {
    this.parent = parent;
    this.name = name;
  }

  @Override
  public boolean isConnected() {
    return parent.isConnected();
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @NotNull
  @Override
  public DatabaseCountEntries countEntries() {
    return parent.countEntries(name);
  }

  @NotNull
  @Override
  public DatabaseQuery query() {
    return parent.query(name);
  }

  @NotNull
  @Override
  public DatabaseUpdate update() {
    return parent.update(name);
  }

  @NotNull
  @Override
  public DatabaseInsertion insert() {
    return parent.insert(name);
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate insertOrUpdate() {
    return parent.insertOrUpdate(name);
  }

  @NotNull
  @Override
  public DatabaseDeletion delete() {
    return parent.delete(name);
  }

  @NotNull
  @Override
  public Database getParent() {
    return parent;
  }

  @Override
  public String toString() {
    return "SpecificDatabase[" + name + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultSpecificDatabase that = (DefaultSpecificDatabase) o;
    return Objects.equals(parent, that.parent) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parent, name);
  }
}
