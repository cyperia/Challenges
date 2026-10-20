package net.cyperia.commons.common.concurrent.cache;

import net.cyperia.commons.common.annotations.ReplaceWith;
import net.cyperia.commons.common.collection.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

/**
 * @see com.google.common.cache.Cache
 * @deprecated Use {@link com.google.common.cache.Cache} instead
 */
@Deprecated
@ReplaceWith("com.google.common.cache.Cache")
public interface ICache<K, V> {

  ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2, new NamedThreadFactory(threadId -> String.format("CacheTask-%s", threadId)));

  boolean contains(@NotNull K key);

  int size();

  default boolean isEmpty() {
    return size() == 0;
  }

  void clear();

  @NotNull
  Map<K, V> values();

  default void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
    values().forEach(action);
  }

}
