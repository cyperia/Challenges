package net.cyperia.commons.common.concurrent.cache;

import net.cyperia.commons.common.annotations.ReplaceWith;
import org.jetbrains.annotations.NotNull;

/**
 * @see com.google.common.cache.LoadingCache
 * @deprecated Use {@link com.google.common.cache.LoadingCache} instead
 */
@Deprecated
@ReplaceWith("com.google.common.cache.LoadingCache")
public interface DatabaseCache<K, V> extends ICache<K, V> {

  @NotNull
  V getData(@NotNull K key);

}
