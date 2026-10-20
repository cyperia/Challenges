package net.cyperia.commons.database.access;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.Propertyable;
import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class CachedDatabaseAccess<V> extends DirectDatabaseAccess<V> {

  protected final Map<String, V> cache = new ConcurrentHashMap<>();

  public CachedDatabaseAccess(@NotNull Database database, @NotNull DatabaseAccessConfig config, @NotNull BiFunction<? super Document, ? super String, ? extends V> mapper) {
    super(database, config, mapper);
  }

  @Nullable
  @Override
  public V getValue(@NotNull String key) throws DatabaseException {
    V value = cache.get(key);
    if (value != null) return value;

    value = super.getValue(key);
    cache.put(key, value);
    return value;
  }

  @NotNull
  @Override
  public V getValue(@NotNull String key, @NotNull V def) throws DatabaseException {
    V value = cache.get(key);
    if (value != null) return value;

    value = super.getValue(key, def);
    cache.put(key, value);
    return value;
  }

  @NotNull
  @Override
  public Optional<V> getValueOptional(@NotNull String key) throws DatabaseException {
    V cached = cache.get(key);
    if (cached != null) return Optional.of(cached);

    return super.getValueOptional(key);
  }

  @Override
  public void setValue(@NotNull String key, @Nullable V value) throws DatabaseException {
    cache.put(key, value);
    super.setValue(key, value);
  }

  @NotNull
  public static CachedDatabaseAccess<String> newStringAccess(@NotNull Database database, @NotNull DatabaseAccessConfig config) {
    return new CachedDatabaseAccess<>(database, config, Propertyable::getString);
  }

  @NotNull
  public static CachedDatabaseAccess<Integer> newIntAccess(@NotNull Database database, @NotNull DatabaseAccessConfig config) {
    return new CachedDatabaseAccess<>(database, config, Propertyable::getInt);
  }

  @NotNull
  public static CachedDatabaseAccess<Long> newLongAccess(@NotNull Database database, @NotNull DatabaseAccessConfig config) {
    return new CachedDatabaseAccess<>(database, config, Propertyable::getLong);
  }

  @NotNull
  public static CachedDatabaseAccess<Double> newDoubleAccess(@NotNull Database database, @NotNull DatabaseAccessConfig config) {
    return new CachedDatabaseAccess<>(database, config, Propertyable::getDouble);
  }

  @NotNull
  public static CachedDatabaseAccess<Document> newDocumentAccess(@NotNull Database database, @NotNull DatabaseAccessConfig config) {
    return new CachedDatabaseAccess<>(database, config, Document::getDocument);
  }

}
