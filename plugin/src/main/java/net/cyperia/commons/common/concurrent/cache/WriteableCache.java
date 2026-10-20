package net.cyperia.commons.common.concurrent.cache;

import net.cyperia.commons.common.annotations.ReplaceWith;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
@ReplaceWith("com.google.common.cache.Cache")
public interface WriteableCache<K, V> extends ICache<K, V> {

  @Nullable
  V getData(@NotNull K key);

  void setData(@NotNull K key, @Nullable V value);

}
