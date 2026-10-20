package net.cyperia.commons.common.config.document;

import net.cyperia.commons.common.config.Config;
import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.misc.ReflectionUtils;
import net.cyperia.commons.common.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractConfig implements Config {

  protected static final ILogger logger = ILogger.forThisClass();

  @NotNull
  protected <T> T getDef(@NotNull T def, @NotNull Supplier<? extends T> getter) {
    T value = getter.get();
    return value == null ? def : value;
  }

  @NotNull
  @Override
  public Object getObject(@NotNull String path, @NotNull Object def) {
    Object value = getObject(path);
    return value == null ? def : value;
  }

  @NotNull
  @Override
  public String getString(@NotNull String path, @NotNull String def) {
    return getDef(def, () -> getString(path));
  }

  @Override
  public char getChar(@NotNull String path) {
    return getChar(path, (char) 0);
  }

  @Override
  public char getChar(@NotNull String path, char def) {
    try {
      return getString(path).charAt(0);
    } catch (NullPointerException | IndexOutOfBoundsException ex) {
      return def;
    }
  }

  @Override
  public double getDouble(@NotNull String path) {
    return getDouble(path, 0);
  }

  @Override
  public float getFloat(@NotNull String path) {
    return getFloat(path, 0);
  }

  @Override
  public long getLong(@NotNull String path) {
    return getLong(path, 0);
  }

  @Override
  public int getInt(@NotNull String path) {
    return getInt(path, 0);
  }

  @Override
  public short getShort(@NotNull String path) {
    return getShort(path, (short) 0);
  }

  @Override
  public byte getByte(@NotNull String path) {
    return getByte(path, (byte) 0);
  }

  @Override
  public boolean getBoolean(@NotNull String path) {
    return getBoolean(path, false);
  }

  @NotNull
  @Override
  public UUID getUUID(@NotNull String path, @NotNull UUID def) {
    return getDef(def, () -> getUUID(path));
  }

  @NotNull
  @Override
  public OffsetDateTime getDateTime(@NotNull String path, @NotNull OffsetDateTime def) {
    return getDef(def, () -> getDateTime(path));
  }

  @NotNull
  @Override
  public Date getDate(@NotNull String path, @NotNull Date def) {
    return getDef(def, () -> getDate(path));
  }

  @NotNull
  @Override
  public Color getColor(@NotNull String path, @NotNull Color def) {
    return getDef(def, () -> getColor(path));
  }

  @Nullable
  @Override
  public <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull Class<E> classOfEnum) {
    return ReflectionUtils.getEnumOrNull(getString(path), classOfEnum);
  }

  @NotNull
  @Override
  public <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull E def) {
    E value = getEnum(path, def.getDeclaringClass());
    return value == null ? def : value;
  }

  @Nullable
  @Override
  public Class<?> getClass(@NotNull String path) {
    try {
      return Class.forName(getString(path));
    } catch (Exception ex) {
      return null;
    }
  }

  @NotNull
  @Override
  public Class<?> getClass(@NotNull String path, @NotNull Class<?> def) {
    return getDef(def, () -> getClass(path));
  }

  @Nullable
  @Override
  public Version getVersion(@NotNull String path) {
    return Version.parse(getString(path), null);
  }

  @NotNull
  @Override
  public Version getVersion(@NotNull String path, @NotNull Version def) {
    return getDef(def, () -> getVersion(path));
  }

  @Nullable
  @Override
  public byte[] getBinary(@NotNull String path) {
    String string = getString(path);
    if (string == null) return null;
    return Base64.getDecoder().decode(string);
  }

  @NotNull
  @Override
  public String[] getStringArray(@NotNull String path) {
    return getStringList(path).toArray(new String[0]);
  }

  @NotNull
  @Override
  public <E extends Enum<E>> List<E> getEnumList(@NotNull String path, @NotNull Class<E> classOfEnum) {
    return mapList(path, name -> ReflectionUtils.getEnumOrNull(name, classOfEnum));
  }

  @NotNull
  @Override
  public List<Character> getCharacterList(@NotNull String path) {
    return mapList(path, string -> string == null || string.length() == 0 ? (char) 0 : string.charAt(0));
  }

  @NotNull
  @Override
  public List<UUID> getUUIDList(@NotNull String path) {
    return mapList(path, UUID::fromString);
  }

  @NotNull
  @Override
  public List<Byte> getByteList(@NotNull String path) {
    return mapList(path, Byte::parseByte);
  }

  @NotNull
  @Override
  public List<Short> getShortList(@NotNull String path) {
    return mapList(path, Short::parseShort);
  }

  @NotNull
  @Override
  public List<Integer> getIntegerList(@NotNull String path) {
    return mapList(path, Integer::parseInt);
  }

  @NotNull
  @Override
  public List<Long> getLongList(@NotNull String path) {
    return mapList(path, Long::parseLong);
  }

  @NotNull
  @Override
  public List<Float> getFloatList(@NotNull String path) {
    return mapList(path, Float::parseFloat);
  }

  @NotNull
  @Override
  public List<Double> getDoubleList(@NotNull String path) {
    return mapList(path, Double::parseDouble);
  }

  @NotNull
  @Override
  public <T> List<T> mapList(@NotNull String path, @NotNull Function<String, ? extends T> mapper) {
    List<String> list = getStringList(path);
    List<T> result = new ArrayList<>(list.size());
    for (String string : list) {
      try {
        result.add(mapper.apply(string));
      } catch (Exception ex) {
        logger.error("Unable to map values for '{}'", string, ex);
      }
    }
    return result;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @NotNull
  @Override
  public Map<String, String> valuesAsStrings() {
    Map<String, String> map = new HashMap<>();
    values().forEach((key, value) -> map.put(key, String.valueOf(value)));
    return map;
  }

  @NotNull
  @Override
  public <K, V> Map<K, V> mapValues(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super String, ? extends V> valueMapper) {
    return map(valuesAsStrings(), keyMapper, valueMapper);
  }

  @NotNull
  @Override
  public Set<Entry<String, Object>> entrySet() {
    return values().entrySet();
  }

  @NotNull
  public <FromK, FromV, ToK, ToV> Map<ToK, ToV> map(@NotNull Map<? extends FromK, ? extends FromV> values,
                                                    @NotNull Function<? super FromK, ? extends ToK> keyMapper,
                                                    @NotNull Function<? super FromV, ? extends ToV> valueMapper) {
    Map<ToK, ToV> result = new HashMap<>();
    values.forEach((key, value) -> {
      try {
        result.put(keyMapper.apply(key), valueMapper.apply(value));
      } catch (Exception ex) {
        logger.error("Unable to map values for '{}'='{}'", key, value, ex);
      }
    });
    return result;
  }

}
