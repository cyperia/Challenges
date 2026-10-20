package net.cyperia.commons.common.concurrent.cache;

import net.cyperia.commons.common.annotations.ReplaceWith;
import net.cyperia.commons.common.collection.pair.Tuple;
import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.misc.SimpleCollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Deprecated
@ReplaceWith("com.google.common.cache.Cache")
public class CleanWriteableCache<K, V> implements WriteableCache<K, V> {

  protected final Map<K, Tuple<Long, V>> cache = new ConcurrentHashMap<>();
  protected final ILogger logger;
  protected final long cleanInterval;
  protected final long unusedTimeBeforeClean;

  public CleanWriteableCache(@Nullable ILogger logger, long unusedTimeBeforeClean, long cleanInterval, @NotNull String taskName) {
    this.logger = logger;
    this.cleanInterval = cleanInterval;
    this.unusedTimeBeforeClean = unusedTimeBeforeClean;

    EXECUTOR.scheduleAtFixedRate(this::cleanCache, cleanInterval, cleanInterval, TimeUnit.MILLISECONDS);
  }

  public void cleanCache() {
    if (logger != null) logger.debug("Cleaning cache");
    long now = System.currentTimeMillis();
    Collection<K> remove = new ArrayList<>();
    cache.forEach((key, pair) -> {
      if (now - pair.getFirst() > unusedTimeBeforeClean) {
        if (logger != null)
          logger.trace("Removing {} from cache, last usage was {}s ago", key, (now - pair.getFirst()) / 1000);
        remove.add(key);
      }
    });
    remove.forEach(cache::remove);
  }

  @Nullable
  @Override
  public V getData(@NotNull K key) {
    Tuple<Long, V> pair = cache.get(key);
    if (pair == null) return null;
    pair.setFirst(System.currentTimeMillis());
    return pair.getSecond();
  }

  @Override
  public void setData(@NotNull K key, @Nullable V value) {
    cache.put(key, new Tuple<>(System.currentTimeMillis(), value));
  }

  @Override
  public boolean contains(@NotNull K key) {
    return cache.containsKey(key);
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @NotNull
  @Override
  public Map<K, V> values() {
    return Collections.unmodifiableMap(SimpleCollectionUtils.convertMap(cache, k -> k, Tuple::getSecond));
  }

}
