package net.cyperia.commons.database.access;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface DatabaseAccess<V> {

  @Nullable
  V getValue(@NotNull String key) throws DatabaseException;

  @NotNull
  V getValue(@NotNull String key, @NotNull V def) throws DatabaseException;

  @NotNull
  Optional<V> getValueOptional(@NotNull String key) throws DatabaseException;

  void setValue(@NotNull String key, @Nullable V value) throws DatabaseException;

  default boolean hasValue(@NotNull String key) throws DatabaseException {
    return getValueOptional(key).isPresent();
  }

  @NotNull
  Database getDatabase();

  @NotNull
  DatabaseAccessConfig getConfig();

}
