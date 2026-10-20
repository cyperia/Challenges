package net.cyperia.commons.database.access;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public class DirectDatabaseAccess<V> implements DatabaseAccess<V> {

  protected final Database database;
  protected final DatabaseAccessConfig config;
  protected final BiFunction<? super Document, ? super String, ? extends V> mapper;

  public DirectDatabaseAccess(@NotNull Database database, @NotNull DatabaseAccessConfig config, @NotNull BiFunction<? super Document, ? super String, ? extends V> mapper) {
    this.database = database;
    this.config = config;
    this.mapper = mapper;
  }

  @Nullable
  @Override
  public V getValue(@NotNull String key) throws DatabaseException {
    return getValue0(key).orElse(null);
  }

  @NotNull
  @Override
  public V getValue(@NotNull String key, @NotNull V def) throws DatabaseException {
    return getValue0(key).orElse(def);
  }

  @NotNull
  @Override
  public Optional<V> getValueOptional(@NotNull String key) throws DatabaseException {
    return getValue0(key);
  }

  @NotNull
  protected Optional<V> getValue0(@NotNull String key) throws DatabaseException {
    return database.query(config.getTable())
      .where(config.getKeyField(), key)
      .execute().first()
      .map(document -> mapper.apply(document, config.getValueField()));
  }

  @Override
  public void setValue(@NotNull String key, @Nullable V value) throws DatabaseException {
    database.insertOrUpdate(config.getTable())
      .set(config.getValueField(), value)
      .where(config.getKeyField(), key)
      .execute();
  }

  @NotNull
  @Override
  public Database getDatabase() {
    return database;
  }

  @NotNull
  @Override
  public DatabaseAccessConfig getConfig() {
    return config;
  }
}
