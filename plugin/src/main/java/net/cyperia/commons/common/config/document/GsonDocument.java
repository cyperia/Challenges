package net.cyperia.commons.common.config.document;

import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.cyperia.commons.common.collection.pair.Pair;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.gson.*;
import net.cyperia.commons.common.misc.BukkitReflectionSerializationUtils;
import net.cyperia.commons.common.misc.FileUtils;
import net.cyperia.commons.common.misc.GsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GsonDocument extends AbstractDocument {

  static {
    GsonBuilder builder = new GsonBuilder()
      .disableHtmlEscaping()
      .registerTypeAdapterFactory(GsonTypeAdapter.newPredictableFactory(BukkitReflectionSerializationUtils::isSerializable, new BukkitReflectionSerializableTypeAdapter()))
      .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Document.class, new DocumentTypeAdapter()))
      .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Pair.class, new PairTypeAdapter()))
      .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Class.class, new ClassTypeAdapter()))
      .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Color.class, new ColorTypeAdapter()));

    GSON = builder.create();
    GSON_PRETTY_PRINT = builder.setPrettyPrinting().create();
  }

  // Not implemented in some versions of gson
  TypeAdapter<Number> NUMBER = new TypeAdapter<Number>() {
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      out.value(value);
    }

    @Override
    public Number read(JsonReader in) throws IOException {
      JsonToken jsonToken = in.peek();
      switch (jsonToken) {
        case NUMBER:
        case STRING:
          return new LazilyParsedNumber(in.nextString());
        case BOOLEAN:
        case NULL:
          in.nextNull();
          return null;
        default:
          throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
      }
    }
  };

  public static final Gson GSON, GSON_PRETTY_PRINT;

  private static boolean cleanupEmptyObjects = false;
  private static boolean cleanupEmptyArrays = false;
  private static boolean writePrettyJson = true;

  public static void setCleanupEmptyArrays(boolean clean) {
    cleanupEmptyArrays = clean;
  }

  public static void setCleanupEmptyObjects(boolean clean) {
    cleanupEmptyObjects = clean;
  }

  public static void setWritePrettyJson(boolean pretty) {
    writePrettyJson = pretty;
  }

  public static boolean isWritePrettyJson() {
    return writePrettyJson;
  }

  @NotNull
  public static List<Document> convertArrayToDocuments(@NotNull JsonArray array) {
    List<Document> list = new ArrayList<>(array.size());
    for (JsonElement element : array) {
      if (!element.isJsonObject()) continue;
      list.add(new GsonDocument(element.getAsJsonObject()));
    }
    return list;
  }

  @NotNull
  public static List<String> convertArrayToStrings(@NotNull JsonArray array) {
    List<String> list = new ArrayList<>(array.size());
    for (JsonElement element : array) {
      if (!element.isJsonPrimitive()) continue;
      list.add(element.getAsString());
    }
    return list;
  }

  protected JsonObject jsonObject;

  public GsonDocument(@NotNull File file) throws IOException {
    this(FileUtils.newBufferedReader(file));
  }

  public GsonDocument(@NotNull Reader reader) throws IOException {
    this(new BufferedReader(reader));
  }

  public GsonDocument(@NotNull BufferedReader reader) throws IOException {
    this(reader.ready() ? GSON.fromJson(reader, JsonObject.class) : new JsonObject());
  }

  public GsonDocument(@NotNull String json) {
    this(GSON.fromJson(json, JsonObject.class));
  }

  public GsonDocument(@NotNull String json, @NotNull Document root, @Nullable Document parent) {
    this(GSON.fromJson(json, JsonObject.class), root, parent);
  }

  public GsonDocument(@Nullable JsonObject jsonObject) {
    this.jsonObject = jsonObject == null ? new JsonObject() : jsonObject;
  }

  public GsonDocument(@Nullable JsonObject jsonObject, @NotNull Document root, @Nullable Document parent) {
    super(root, parent);
    this.jsonObject = jsonObject == null ? new JsonObject() : jsonObject;
  }

  public GsonDocument(@NotNull Map<String, Object> values) {
    this();
    GsonUtils.setDocumentProperties(GSON, jsonObject, values);
  }

  public GsonDocument() {
    this(new JsonObject());
  }

  public GsonDocument(@NotNull Object object) {
    this(GSON.toJsonTree(object).getAsJsonObject());
  }

  @Nullable
  @Override
  public String getString(@NotNull String path) {
    JsonElement element = getElement(path).orElse(null);
    return GsonUtils.convertJsonElementToString(element);
  }

  @Nullable
  @Override
  public Object getObject(@NotNull String path) {
    JsonElement element = getElement(path).orElse(null);
    return GsonUtils.unpackJsonElement(element);
  }

  @Nullable
  @Override
  public <T> T getInstance(@NotNull String path, @NotNull Class<T> classOfType) {
    JsonElement element = getElement(path).orElse(null);
    return GSON.fromJson(element, classOfType);
  }

  @Override
  public <T> T toInstanceOf(@NotNull Class<T> classOfT) {
    if (isEmpty()) return null;
    return GSON.fromJson(jsonObject, classOfT);
  }

  @Nullable
  @Override
  public <T> T getSerializable(@NotNull String path, @NotNull Class<T> classOfT) {
    return getInstance(path, classOfT);
  }

  @NotNull
  @Override
  public Document getDocument0(@NotNull String path, @NotNull Document root, @Nullable Document parent) {
    JsonElement element = getElement(path).orElse(null);
    if (element == null || !element.isJsonObject()) setElement(path, element = new JsonObject());
    return new GsonDocument(element.getAsJsonObject(), root, parent);
  }

  @NotNull
  @Override
  public List<Document> getDocumentList(@NotNull String path) {
    JsonElement element = getElement(path).orElse(new JsonArray());
    if (element.isJsonNull()) return new ArrayList<>();
    JsonArray array = element.getAsJsonArray();
    List<Document> documents = new ArrayList<>(array.size());
    for (JsonElement current : array) {
      if (current == null || current.isJsonNull()) documents.add(new GsonDocument((JsonObject) null, root, this));
      else if (current.isJsonObject()) documents.add(new GsonDocument(current.getAsJsonObject(), root, this));
    }
    return documents;
  }

  @Override
  public char getChar(@NotNull String path) {
    return getChar(path, (char) 0);
  }

  @Override
  public char getChar(@NotNull String path, char def) {
    return getPrimitive(path).map(JsonPrimitive::getAsCharacter).orElse(def);
  }

  @Override
  public long getLong(@NotNull String path, long def) {
    return getPrimitive(path).map(JsonPrimitive::getAsLong).orElse(def);
  }

  @Override
  public int getInt(@NotNull String path, int def) {
    return getPrimitive(path).map(JsonPrimitive::getAsInt).orElse(def);
  }

  @Override
  public short getShort(@NotNull String path, short def) {
    return getPrimitive(path).map(JsonPrimitive::getAsShort).orElse(def);
  }

  @Override
  public byte getByte(@NotNull String path, byte def) {
    return getPrimitive(path).map(JsonPrimitive::getAsByte).orElse(def);
  }

  @Override
  public double getDouble(@NotNull String path, double def) {
    return getPrimitive(path).map(JsonPrimitive::getAsDouble).orElse(def);
  }

  @Override
  public float getFloat(@NotNull String path, float def) {
    return getPrimitive(path).map(JsonPrimitive::getAsFloat).orElse(def);
  }

  @Override
  public boolean getBoolean(@NotNull String path, boolean def) {
    return getPrimitive(path).map(JsonPrimitive::getAsBoolean).orElse(def);
  }

  @NotNull
  @Override
  public List<String> getStringList(@NotNull String path) {
    JsonElement element = getElement(path).orElse(null);
    if (element == null || element.isJsonNull()) return new ArrayList<>();
    if (element.isJsonPrimitive())
      return new ArrayList<>(Collections.singletonList(GsonUtils.convertJsonElementToString(element)));
    if (element.isJsonObject())
      throw new IllegalStateException("Cannot extract list out of json object at '" + path + "'");
    return GsonUtils.convertJsonArrayToStringList(element.getAsJsonArray());
  }

  @Nullable
  @Override
  public UUID getUUID(@NotNull String path) {
    return getInstance(path, UUID.class);
  }

  @Nullable
  @Override
  public Date getDate(@NotNull String path) {
    return getInstance(path, Date.class);
  }

  @Nullable
  @Override
  public OffsetDateTime getDateTime(@NotNull String path) {
    return getInstance(path, OffsetDateTime.class);
  }

  @Nullable
  @Override
  public Color getColor(@NotNull String path) {
    return getInstance(path, Color.class);
  }

  @Nullable
  @Override
  public <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull Class<E> classOfEnum) {
    return getInstance(path, classOfEnum);
  }

  @Override
  public boolean isList(@NotNull String path) {
    return checkElement(path, JsonElement::isJsonArray);
  }

  @Override
  public boolean isDocument(@NotNull String path) {
    return checkElement(path, JsonElement::isJsonObject);
  }

  @Override
  public boolean isObject(@NotNull String path) {
    return checkElement(path, JsonElement::isJsonPrimitive);
  }

  private boolean checkElement(@NotNull String path, @NotNull Function<? super JsonElement, Boolean> check) {
    return getElement(path).map(check).orElse(false);
  }

  @Override
  public boolean contains(@NotNull String path) {
    return getElement(path).isPresent();
  }

  @Override
  public int size() {
    return jsonObject.size();
  }

  @Override
  public void set0(@NotNull String path, @Nullable Object value) {
    setElement(path, value);
  }

  @NotNull
  @Override
  public Document set(@NotNull Object object) {
    JsonObject json = GSON.toJsonTree(object).getAsJsonObject();
    for (Entry<String, JsonElement> entry : json.entrySet()) {
      jsonObject.add(entry.getKey(), entry.getValue());
    }
    return this;
  }

  @Override
  public void clear0() {
    // Replacing jsonObject with a new instance would detach a child document from its parent's tree,
    // since the parent still holds a reference to the old one; clearing the live map keeps it attached.
    jsonObject.entrySet().clear();
  }

  @Override
  public void remove0(@NotNull String path) {
    setElement(path, null);
  }

  @NotNull
  @Override
  public Map<String, Object> values() {
    return GsonUtils.convertJsonObjectToMap(jsonObject);
  }

  @Override
  public void forEach(@NotNull BiConsumer<? super String, ? super Object> action) {
    values().forEach(action);
  }

  @NotNull
  @Override
  public Collection<String> keys() {
    try {
      // JsonObject#keySet was added in a later version
      return jsonObject.keySet();
    } catch (Throwable ignored) {
    }

    Collection<String> keys = new ArrayList<>(size());
    for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      keys.add(entry.getKey());
    }
    return keys;
  }

  @NotNull
  private Optional<JsonPrimitive> getPrimitive(@NotNull String path) {
    return getElement(path)
      .filter(JsonPrimitive.class::isInstance)
      .map(JsonPrimitive.class::cast);
  }

  @NotNull
  private Optional<JsonElement> getElement(@NotNull String path) {
    return getElement(path, jsonObject);
  }

  @NotNull
  private Optional<JsonElement> getElement(@NotNull String path, @NotNull JsonObject object) {

    JsonElement fullPathElement = object.get(path);
    if (fullPathElement != null && !fullPathElement.isJsonNull()) return Optional.of(fullPathElement);

    int index = path.indexOf('.');
    if (index == -1) return Optional.empty();

    String child = path.substring(0, index);
    String newPath = path.substring(index + 1);

    JsonElement element = object.get(child);
    if (element == null || element.isJsonNull()) return Optional.empty();

    return getElement(newPath, element.getAsJsonObject());

  }

  private void setElement(@NotNull String path, @Nullable Object value) {

    LinkedList<String> paths = determinePath(path);
    JsonObject object = jsonObject;

    for (int i = 0; i < paths.size() - 1; i++) {

      String current = paths.get(i);
      JsonElement element = object.get(current);
      if (element == null || element.isJsonNull()) {
        if (value == null) return; // There's noting to remove
        object.add(current, element = new JsonObject());
      }

      if (!element.isJsonObject())
        throw new IllegalArgumentException("Cannot replace '" + current + "' on '" + path + "'; It's not an object (" + element.getClass().getName() + ")");
      object = element.getAsJsonObject();

    }

    String lastPath = paths.getLast();
    JsonElement jsonValue =
      value instanceof JsonElement ? (JsonElement) value
        : value == null ? JsonNull.INSTANCE
          : value instanceof Number ? NUMBER.toJsonTree((Number) value)
            : value instanceof Boolean ? TypeAdapters.BOOLEAN.toJsonTree((boolean) value)
              : value instanceof Character ? TypeAdapters.CHARACTER.toJsonTree((char) value)
                : GSON.toJsonTree(value);
    object.add(lastPath, jsonValue);

  }

  @NotNull
  private LinkedList<String> determinePath(@NotNull String path) {

    LinkedList<String> paths = new LinkedList<>();
    String pathCopy = path;
    int index;
    while ((index = pathCopy.indexOf('.')) != -1) {
      String child = pathCopy.substring(0, index);
      pathCopy = pathCopy.substring(index + 1);
      paths.add(child);
    }
    paths.add(pathCopy);

    return paths;

  }

  @NotNull
  @Override
  public String toJson() {
    return GSON.toJson(jsonObject);
  }

  @NotNull
  @Override
  public String toPrettyJson() {
    return GSON_PRETTY_PRINT.toJson(jsonObject);
  }

  @Override
  public String toString() {
    return toJson();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GsonDocument that = (GsonDocument) o;
    return jsonObject.equals(that.jsonObject);
  }

  @Override
  public int hashCode() {
    return jsonObject.hashCode();
  }

  @Override
  public void write(@NotNull Writer writer) throws IOException {
    cleanup();
    (writePrettyJson ? GSON_PRETTY_PRINT : GSON).toJson(jsonObject, writer);
  }

  @NotNull
  public JsonObject getJsonObject() {
    return jsonObject;
  }

  @Override
  public boolean isReadonly() {
    return false;
  }

  public void cleanup() {
    cleanup(jsonObject);
  }

  public static void cleanup(@NotNull JsonObject jsonObject) {
    Iterator<Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, JsonElement> entry = iterator.next();
      JsonElement value = entry.getValue();
      if (value.isJsonObject()) cleanup(value.getAsJsonObject());
//			if (cleanupNulls && value.isJsonNull()) iterator.remove();
      if (cleanupEmptyObjects && value.isJsonObject() && value.getAsJsonObject().size() == 0)
        iterator.remove();
      if (cleanupEmptyArrays && value.isJsonArray() && value.getAsJsonArray().size() == 0) iterator.remove();
    }
  }

}
