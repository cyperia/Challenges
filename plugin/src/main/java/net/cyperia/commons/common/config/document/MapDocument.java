package net.cyperia.commons.common.config.document;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.PropertyHelper;
import net.cyperia.commons.common.misc.GsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This class is not thread safe.
 * Documents of this type are not able to be saved or loaded.
 */
public class MapDocument extends AbstractDocument {

  private final Map<String, Object> values;

  public MapDocument(@NotNull Map<String, Object> values) {
    Preconditions.checkNotNull(values, "Map cannot be null");
    this.values = values;
  }

  public MapDocument(@NotNull Map<String, Object> values, @NotNull Document root, @Nullable Document parent) {
    super(root, parent);
    this.values = values;
  }

  public MapDocument() {
    this(new LinkedHashMap<>());
  }

  @Override
  public boolean isReadonly() {
    return false;
  }

  @NotNull
  @Override
  public Document getDocument0(@NotNull String path, @NotNull Document root, @Nullable Document parent) {
    Object value = this.values.computeIfAbsent(path, key -> new HashMap<>());
    if (value instanceof Map) return new MapDocument((Map<String, Object>) value, root, parent);
    if (value instanceof Document) return (Document) value;
    if (value instanceof String) return new GsonDocument((String) value, root, parent);
    throw new IllegalStateException("Expected java.util.Map, found " + values.getClass().getName());
  }

  @NotNull
  @Override
  public List<Document> getDocumentList(@NotNull String path) {
    List<Document> documents = new ArrayList<>();
    Object value = values.get(path);
    if (value instanceof List) {
      List<Object> list = (List<Object>) value;
      for (Object object : list) {
        if (object instanceof Map) documents.add(new MapDocument((Map<String, Object>) object, root, parent));
        if (object instanceof Document) documents.add((Document) object);
        if (object instanceof String) documents.add(new GsonDocument((String) object, root, this));
      }
    }
    return documents;
  }

  @Override
  public void set0(@NotNull String path, @Nullable Object value) {
    values.put(path, value);
  }

  @Override
  public void clear0() {
    values.clear();
  }

  @Override
  public void remove0(@NotNull String path) {
    values.remove(path);
  }

  @Override
  public void write(@NotNull Writer writer) throws IOException {
    new GsonDocument(values).write(writer);
  }

  @NotNull
  @Override
  public String toJson() {
    return new GsonDocument(values).toJson();
  }

  @NotNull
  @Override
  public String toPrettyJson() {
    return new GsonDocument(values).toPrettyJson();
  }

  @Nullable
  @Override
  public Object getObject(@NotNull String path) {
    return values.get(path);
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
    Object value = values.get(path);
    return value == null ? null : value.toString();
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
    try {
      if (!contains(path)) return def;
      switch (getString(path).toLowerCase()) {
        case "true":
        case "1":
          return true;
        default:
          return false;
      }
    } catch (Exception ex) {
      return def;
    }
  }

  @NotNull
  @Override
  public List<String> getStringList(@NotNull String path) {
    Object object = getObject(path);
    if (object == null) return Collections.emptyList();
    if (object instanceof Iterable)
      return StreamSupport.stream(((Iterable<?>) object).spliterator(), false).map(String::valueOf).collect(Collectors.toList());
    if (object instanceof String)
      return GsonUtils.convertJsonArrayToStringList(GsonDocument.GSON.fromJson((String) object, JsonArray.class));
    throw new IllegalStateException("Cannot convert " + object.getClass() + " to a list");
  }

  @Nullable
  @Override
  public UUID getUUID(@NotNull String path) {
    try {
      Object object = getObject(path);
      if (object instanceof UUID) return (UUID) object;
      return UUID.fromString(String.valueOf(object));
    } catch (Exception ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public Date getDate(@NotNull String path) {
    Object object = getObject(path);
    if (object instanceof String) return PropertyHelper.parseDate((String) object);
    if (object instanceof Date) return (Date) object;
    return null;
  }

  @Nullable
  @Override
  public OffsetDateTime getDateTime(@NotNull String path) {
    Object object = getObject(path);
    if (object instanceof CharSequence) return OffsetDateTime.parse((CharSequence) object);
    if (object instanceof OffsetDateTime) return (OffsetDateTime) object;
    return null;
  }

  @Nullable
  @Override
  public Color getColor(@NotNull String path) {
    Object object = getObject(path);
    if (object instanceof Color) return (Color) object;
    if (object instanceof String) return Color.decode((String) object);
    return null;
  }

  @Override
  public boolean isList(@NotNull String path) {
    Object value = values.get(path);
    return value instanceof Iterable || (value != null && value.getClass().isArray());
  }

  @Override
  public boolean isObject(@NotNull String path) {
    return !isDocument(path) && !isList(path);
  }

  @Override
  public boolean isDocument(@NotNull String path) {
    Object value = values.get(path);
    return value instanceof Map || value instanceof Document;
  }

  @Override
  public boolean contains(@NotNull String path) {
    return values.containsKey(path);
  }

  @Override
  public int size() {
    return values.size();
  }

  @NotNull
  @Override
  public Map<String, Object> values() {
    return Collections.unmodifiableMap(values);
  }

  @NotNull
  @Override
  public Collection<String> keys() {
    return values.keySet();
  }

  @Override
  public void forEach(@NotNull BiConsumer<? super String, ? super Object> action) {
    values.forEach(action);
  }

}
