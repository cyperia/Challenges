package net.cyperia.commons.common.config;

import net.cyperia.commons.common.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @see Document
 * @see Config
 */
public interface Propertyable {

  <T> T getInstance(@NotNull String path, @NotNull Class<T> classOfT);

  @Nullable
  Object getObject(@NotNull String path);

  @NotNull
  Object getObject(@NotNull String path, @NotNull Object def);

  @NotNull
  @SuppressWarnings("unchecked")
  default <T, O extends Propertyable> Optional<T> getOptional(@NotNull String key, @NotNull BiFunction<O, ? super String, ? extends T> extractor) {
    return Optional.ofNullable(extractor.apply((O) this, key));
  }

  @NotNull
  @SuppressWarnings("unchecked")
  default <O extends Propertyable> Propertyable apply(@NotNull Consumer<O> action) {
    action.accept((O) this);
    return this;
  }

  @NotNull
  default <O extends Propertyable> Propertyable applyIf(boolean expression, @NotNull Consumer<O> action) {
    if (expression)
      apply(action);
    return this;
  }

  @Nullable
  String getString(@NotNull String path);

  @NotNull
  String getString(@NotNull String path, @NotNull String def);

  @Nullable
  byte[] getBinary(@NotNull String path);

  char getChar(@NotNull String path);

  char getChar(@NotNull String path, char def);

  long getLong(@NotNull String path);

  long getLong(@NotNull String path, long def);

  int getInt(@NotNull String path);

  int getInt(@NotNull String path, int def);

  short getShort(@NotNull String path);

  short getShort(@NotNull String path, short def);

  byte getByte(@NotNull String path);

  byte getByte(@NotNull String path, byte def);

  float getFloat(@NotNull String path);

  float getFloat(@NotNull String path, float def);

  double getDouble(@NotNull String path);

  double getDouble(@NotNull String path, double def);

  boolean getBoolean(@NotNull String path);

  boolean getBoolean(@NotNull String path, boolean def);

  @NotNull
  List<String> getStringList(@NotNull String path);

  @NotNull
  String[] getStringArray(@NotNull String path);

  @NotNull
  <E extends Enum<E>> List<E> getEnumList(@NotNull String path, @NotNull Class<E> classOfEnum);

  @NotNull
  List<UUID> getUUIDList(@NotNull String path);

  @NotNull
  List<Character> getCharacterList(@NotNull String path);

  @NotNull
  List<Byte> getByteList(@NotNull String path);

  @NotNull
  List<Short> getShortList(@NotNull String path);

  @NotNull
  List<Integer> getIntegerList(@NotNull String path);

  @NotNull
  List<Long> getLongList(@NotNull String path);

  @NotNull
  List<Float> getFloatList(@NotNull String path);

  @NotNull
  List<Double> getDoubleList(@NotNull String path);

  @Nullable
  UUID getUUID(@NotNull String path);

  @NotNull
  UUID getUUID(@NotNull String path, @NotNull UUID def);

  @Nullable
  OffsetDateTime getDateTime(@NotNull String path);

  @NotNull
  OffsetDateTime getDateTime(@NotNull String path, @NotNull OffsetDateTime def);

  @Nullable
  Date getDate(@NotNull String path);

  @NotNull
  Date getDate(@NotNull String path, @NotNull Date def);

  @Nullable
  Color getColor(@NotNull String path);

  @NotNull
  Color getColor(@NotNull String path, @NotNull Color def);

  @Nullable
  <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull Class<E> classOfEnum);

  @NotNull
  <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull E def);

  @Nullable
  Class<?> getClass(@NotNull String path);

  @NotNull
  Class<?> getClass(@NotNull String path, @NotNull Class<?> def);

  @Nullable
  Version getVersion(@NotNull String path);

  @NotNull
  Version getVersion(@NotNull String path, @NotNull Version def);

  boolean isList(@NotNull String path);

  boolean isObject(@NotNull String path);

  boolean contains(@NotNull String path);

  boolean isEmpty();

  int size();

  @NotNull
  Map<String, Object> values();

  @NotNull
  Map<String, String> valuesAsStrings();

  @NotNull
  <K, V> Map<K, V> mapValues(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super String, ? extends V> valueMapper);

  @NotNull
  <T> List<T> mapList(@NotNull String path, @NotNull Function<String, ? extends T> mapper);

  @NotNull
  Collection<String> keys();

  @NotNull
  Set<Entry<String, Object>> entrySet();

  void forEach(@NotNull BiConsumer<? super String, ? super Object> action);

}
