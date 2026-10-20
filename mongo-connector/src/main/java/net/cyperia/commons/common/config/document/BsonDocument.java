package net.cyperia.commons.common.config.document;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.misc.FileUtils;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.json.JsonWriterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

public class BsonDocument extends AbstractDocument {

  protected org.bson.Document bsonDocument;

  public BsonDocument(@NotNull File file) throws IOException {
    this(FileUtils.newBufferedReader(file));
  }

  public BsonDocument(@NotNull Reader reader) {
    BufferedReader buffered = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    StringBuilder content = new StringBuilder();
    buffered.lines().forEach(content::append);
    bsonDocument = org.bson.Document.parse(content.toString());
  }

  public BsonDocument(@NotNull org.bson.Document bsonDocument) {
    this.bsonDocument = bsonDocument;
  }

  public BsonDocument(@NotNull org.bson.Document bsonDocument, @NotNull Document root, @Nullable Document parent) {
    super(root, parent);
    this.bsonDocument = bsonDocument;
  }

  public BsonDocument() {
    this(new org.bson.Document());
  }

  @NotNull
  @Override
  public Document getDocument0(@NotNull String path, @NotNull Document root, @Nullable Document parent) {
    org.bson.Document document = bsonDocument.get(path, org.bson.Document.class);
    if (document == null) {
      bsonDocument.put(path, document = new org.bson.Document());
    }

    return new BsonDocument(document, root, parent);
  }

  @NotNull
  @Override
  public List<Document> getDocumentList(@NotNull String path) {
    BsonArray array = bsonDocument.get(path, BsonArray.class);
    if (array == null) return new ArrayList<>();
    List<Document> documents = new ArrayList<>(array.size());
    for (BsonValue value : array) {
      if (!value.isDocument()) continue;
      String json = value.asDocument().toJson();
      org.bson.Document document = org.bson.Document.parse(json);
      documents.add(new BsonDocument(document, root, this));
    }
    return documents;
  }

  @Nullable
  @Override
  public String getString(@NotNull String path) {
    Object value = getObject(path);
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
  public double getDouble(@NotNull String path, double def) {
    try {
      return Double.parseDouble(getString(path));
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
  public boolean getBoolean(@NotNull String path, boolean def) {
    try {
      Object value = bsonDocument.get(path);
      if (value instanceof Boolean) return (Boolean) value;
      if (value instanceof String) return Boolean.parseBoolean((String) value);
    } catch (Exception ex) {
    }
    return def;
  }

  @Nullable
  @Override
  public Object getObject(@NotNull String path) {
    return bsonDocument.get(path);
  }

  @NotNull
  @Override
  public List<String> getStringList(@NotNull String path) {
    return bsonDocument.getList(path, String.class);
  }

  @Nullable
  @Override
  public UUID getUUID(@NotNull String path) {
    try {
      Object value = bsonDocument.get(path);
      if (value instanceof UUID) return (UUID) value;
      if (value instanceof String) return UUID.fromString((String) value);
    } catch (Exception ex) {
    }
    return null;
  }

  @Nullable
  @Override
  public Date getDate(@NotNull String path) {
    return bsonDocument.getDate(path);
  }

  @Nullable
  @Override
  public OffsetDateTime getDateTime(@NotNull String path) {
    Object value = getObject(path);

    if (value == null)
      return null;
    if (value instanceof OffsetDateTime)
      return (OffsetDateTime) value;
    if (value instanceof Date)
      return ((Date) value).toInstant().atOffset(ZoneOffset.UTC);
    if (value instanceof String)
      return OffsetDateTime.parse((CharSequence) value);

    throw new IllegalStateException(value.getClass().getName() + " cannot be converted to java.time.OffsetDateTime");
  }

  @Nullable
  @Override
  public Color getColor(@NotNull String path) {
    Object value = getObject(path);

    if (value == null)
      return null;
    if (value instanceof Color)
      return (Color) value;
    if (value instanceof String)
      return Color.decode((String) value);

    throw new IllegalStateException(value.getClass().getName() + " cannot be converted to java.awt.Color");
  }

  @Override
  public <T> T getInstance(@NotNull String path, @NotNull Class<T> classOfT) {
    return copyJson().getInstance(path, classOfT);
  }

  @Override
  public <T> T toInstanceOf(@NotNull Class<T> classOfT) {
    return copyJson().toInstanceOf(classOfT);
  }

  @Override
  public boolean contains(@NotNull String path) {
    return bsonDocument.containsKey(path);
  }

  @Override
  public boolean isList(@NotNull String path) {
    Object value = bsonDocument.get(path);
    return value instanceof Iterable || (value != null && value.getClass().isArray());
  }

  @Override
  public boolean isDocument(@NotNull String path) {
    Object value = bsonDocument.get(path);
    return value instanceof org.bson.Document;
  }

  @Override
  public boolean isObject(@NotNull String path) {
    return !isList(path) && !isDocument(path);
  }

  @Override
  public int size() {
    return bsonDocument.size();
  }

  @Override
  public void clear0() {
    bsonDocument.clear();
  }

  @Override
  public void set0(@NotNull String path, @Nullable Object value) {
    bsonDocument.put(path, value);
  }

  @Override
  public void remove0(@NotNull String path) {
    bsonDocument.remove(path);
  }

  @Override
  public void write(@NotNull Writer writer) throws IOException {
    String json = bsonDocument.toString();
    writer.write(json);
  }

  @NotNull
  @Override
  public Map<String, Object> values() {
    return Collections.unmodifiableMap(bsonDocument);
  }

  @NotNull
  @Override
  public Collection<String> keys() {
    return bsonDocument.keySet();
  }

  @Override
  public void forEach(@NotNull BiConsumer<? super String, ? super Object> action) {
    bsonDocument.forEach(action);
  }

  @NotNull
  @Override
  public String toJson() {
    return bsonDocument.toJson();
  }

  @NotNull
  @Override
  public String toPrettyJson() {
    return bsonDocument.toJson(JsonWriterSettings.builder().indent(true).build());
  }

  @Override
  public String toString() {
    return toJson();
  }

  @Override
  public boolean isReadonly() {
    return false;
  }

}
