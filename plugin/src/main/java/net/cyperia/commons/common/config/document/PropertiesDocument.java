package net.cyperia.commons.common.config.document;

import net.cyperia.commons.common.collection.Colors;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.PropertyHelper;
import net.cyperia.commons.common.misc.FileUtils;
import net.cyperia.commons.common.misc.PropertiesUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * This document only supports basic objects like {@link Number numbers}, {@link String strings}, {@link Character characters} and {@link Boolean booleans}.
 * You may use more advanced documents which are fully supported like {@link GsonDocument}
 */
public class PropertiesDocument extends AbstractDocument {

  protected final Properties properties;

  public PropertiesDocument(@Nullable Properties properties) {
    this.properties = properties == null ? new Properties() : properties;
  }

  public PropertiesDocument(@NotNull File file) throws IOException {
    properties = new Properties();
    properties.load(FileUtils.newBufferedReader(file));
  }

  @NotNull
  @Override
  public Document getDocument0(@NotNull String path, @NotNull Document root, @Nullable Document parent) {
    throw new UnsupportedOperationException("PropertiesDocument.getDocument(String)");
  }

  @NotNull
  @Override
  public List<Document> getDocumentList(@NotNull String path) {
    throw new UnsupportedOperationException("PropertiesDocument.getDocumentList(String)");
  }

  @NotNull
  @Override
  public List<String> getStringList(@NotNull String path) {
    throw new UnsupportedOperationException("PropertiesDocument.getList(String)");
  }

  @Nullable
  @Override
  public Object getObject(@NotNull String path) {
    return properties.get(path);
  }

  @Override
  public <T> T getInstance(@NotNull String path, @NotNull Class<T> classOfT) {
    return classOfT.cast(getObject(path));
  }

  @Override
  public <T> T toInstanceOf(@NotNull Class<T> classOfT) {
    return copyJson().toInstanceOf(classOfT);
  }

  @Nullable
  @Override
  public String getString(@NotNull String path) {
    return properties.getProperty(path);
  }

  @Override
  public long getLong(@NotNull String path, long def) {
    try {
      return Long.parseLong(getString(path));
    } catch (Exception ex) {
      return def;
    }
  }

  @Override
  public int getInt(@NotNull String path, int def) {
    try {
      return Integer.parseInt(getString(path));
    } catch (Exception ex) {
      return def;
    }
  }

  @Override
  public short getShort(@NotNull String path, short def) {
    try {
      return Short.parseShort(getString(path));
    } catch (Exception ex) {
      return def;
    }
  }

  @Override
  public byte getByte(@NotNull String path, byte def) {
    try {
      return Byte.parseByte(getString(path));
    } catch (Exception ex) {
      return def;
    }
  }

  @Override
  public float getFloat(@NotNull String path, float def) {
    try {
      return Float.parseFloat(getString(path));
    } catch (Exception ex) {
      return def;
    }
  }

  @Override
  public double getDouble(@NotNull String path, double def) {
    try {
      return Double.parseDouble(getString(path));
    } catch (Exception ex) {
      return def;
    }
  }

  @Override
  public boolean getBoolean(@NotNull String path, boolean def) {
    if (!contains(path)) return def;
    return Boolean.parseBoolean(getString(path));
  }

  @Nullable
  @Override
  public UUID getUUID(@NotNull String path) {
    try {
      return UUID.fromString(getString(path));
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public Date getDate(@NotNull String path) {
    return PropertyHelper.parseDate(getString(path));
  }

  @Nullable
  @Override
  public OffsetDateTime getDateTime(@NotNull String path) {
    try {
      return OffsetDateTime.parse(getString(path));
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public Color getColor(@NotNull String path) {
    String string = getString(path);
    return string == null ? null : Color.decode(string);
  }

  @Nullable
  @Override
  public <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull Class<E> classOfEnum) {
    try {
      String name = getString(path);
      if (name == null) return null;
      return Enum.valueOf(classOfEnum, name);
    } catch (Throwable ex) {
      return null;
    }
  }

  @Override
  public boolean contains(@NotNull String path) {
    return properties.containsKey(path);
  }

  @Override
  public boolean isList(@NotNull String path) {
    return false;
  }

  @Override
  public boolean isObject(@NotNull String path) {
    return true;
  }

  @Override
  public boolean isDocument(@NotNull String path) {
    return false;
  }

  @Override
  public int size() {
    return properties.size();
  }

  @NotNull
  @Override
  public Map<String, Object> values() {
    Map<String, Object> map = new LinkedHashMap<>();
    for (Entry<Object, Object> entry : properties.entrySet()) {
      map.put((String) entry.getKey(), entry.getValue());
    }
    return map;
  }

  @NotNull
  @Override
  public Collection<String> keys() {
    return properties.stringPropertyNames();
  }

  @Override
  public void forEach(@NotNull BiConsumer<? super String, ? super Object> action) {
    values().forEach(action);
  }

  @Override
  public void set0(@NotNull String path, @Nullable Object value) {
    final String asString;
    if (value instanceof Color) {
      asString = Colors.asHex((Color) value);
    } else {
      asString = String.valueOf(value);
    }

    properties.setProperty(path, asString);
  }

  @Override
  public void clear0() {
    properties.clear();
  }

  @Override
  public void remove0(@NotNull String path) {
    properties.remove(path);
  }

  @Override
  public void write(@NotNull Writer writer) throws IOException {
    properties.store(writer, null);
  }

  @NotNull
  public Properties getProperties() {
    return properties;
  }

  @NotNull
  @Override
  public String toJson() {
    return copyJson().toJson();
  }

  @NotNull
  @Override
  public String toPrettyJson() {
    return copyJson().toPrettyJson();
  }

  @NotNull
  @Override
  public Document copyJson() {
    Map<String, Object> map = new HashMap<>();
    PropertiesUtils.setProperties(properties, map);
    return new GsonDocument(map);
  }

  @Override
  public boolean isReadonly() {
    return false;
  }
}
