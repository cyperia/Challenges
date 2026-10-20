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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Deprecated
@ReplaceWith("com.google.common.cache.LoadingCache")
public class CleanAndWriteDatabaseCache<K, V> implements DatabaseCache<K, V> {

  protected final Map<K, Tuple<Long, V>> cache = new ConcurrentHashMap<>();
  protected final Predicate<? super V> check;
  protected final Function<? super K, ? extends V> query;
  protected final Function<? super K, ? extends V> fallback;
  protected final BiConsumer<? super K, ? super V> writer;
  protected final long unusedTimeBeforeClean;
  protected final long cleanAndWriteInterval;
  protected final ILogger logger;

  public CleanAndWriteDatabaseCache(@Nullable ILogger logger, long unusedTimeBeforeClean, long cleanAndWriteInterval, @NotNull String taskName,
                                    @NotNull Predicate<? super V> check, @NotNull Function<? super K, ? extends V> fallback,
                                    @NotNull Function<? super K, ? extends V> query, @NotNull BiConsumer<? super K, ? super V> writer) {
    this.logger = logger;
    this.unusedTimeBeforeClean = unusedTimeBeforeClean;
    this.cleanAndWriteInterval = cleanAndWriteInterval;
    this.check = check;
    this.query = query;
    this.fallback = fallback;
    this.writer = writer;

    EXECUTOR.scheduleAtFixedRate(this::writeCache, cleanAndWriteInterval, cleanAndWriteInterval, TimeUnit.MILLISECONDS);
    Runtime.getRuntime().addShutdownHook(new Thread(this::writeCache));
  }

  public void writeCache() {
    if (logger != null) logger.debug("Writing & Cleaning cache");
    cleanAndWrite(cache, unusedTimeBeforeClean, logger, check, writer);
  }

  @NotNull
  @Override
  public V getData(@NotNull K key) {
    Tuple<Long, V> cached = cache.get(key);
    if (cached != null) {
      cached.setFirst(System.currentTimeMillis());
      return cached.getSecond();
    }

    try {
      V data = query.apply(key);
      if (logger != null) logger.trace("Queried data {} for {}", data, key);
      cache.put(key, new Tuple<>(System.currentTimeMillis(), data));
      return data;
    } catch (Exception ex) {
      if (logger != null) logger.error("Could not get data for {}", key, ex);
      return fallback.apply(key);
    }
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

  public static <K, V> void cleanAndWrite(@NotNull Map<K, Tuple<Long, V>> cache, long unusedTimeBeforeClean, @Nullable ILogger logger,
                                          @NotNull Predicate<? super V> check, @NotNull BiConsumer<? super K, ? super V> writer) {
    long now = System.currentTimeMillis();
    Collection<K> remove = new ArrayList<>();
    cache.forEach((key, pair) -> {
      try {
        if (now - pair.getFirst() > unusedTimeBeforeClean) {
          if (logger != null)
            logger.trace("Removing {} from cache, last usage was {}s ago", key, (now - pair.getFirst()) / 1000);
          remove.add(key);
        }

        V value = pair.getSecond();
        if (!check.test(value)) return;

        if (logger != null) logger.trace("Writing {}", value);
        writer.accept(key, value);
      } catch (Exception ex) {
        if (logger != null) logger.error("Unable to write cache for {}", key, ex);
      }
    });
    remove.forEach(cache::remove);
  }

  @NotNull
  @Override
  public Map<K, V> values() {
    return Collections.unmodifiableMap(SimpleCollectionUtils.convertMap(cache, k -> k, Tuple::getSecond));
  }

}
