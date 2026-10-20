package net.cyperia.commons.common.config.document;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.Propertyable;
import net.cyperia.commons.common.config.exceptions.ConfigReadOnlyException;
import net.cyperia.commons.common.version.Version;
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
import java.util.function.BiFunction;
import java.util.function.Function;

public class EmptyDocument implements Document {

  public static final EmptyDocument ROOT = new EmptyDocument();

  protected final Document root, parent;

  public EmptyDocument(@NotNull Document root, @Nullable Document parent) {
    this.root = root;
    this.parent = parent;
  }

  public EmptyDocument() {
    this.root = this;
    this.parent = null;
  }

  @NotNull
  @Override
  public Document getDocument(@NotNull String path) {
    return new EmptyDocument();
  }

  @NotNull
  @Override
  public <R> R mapDocument(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper) {
    return mapper.apply(Document.empty());
  }

  @Nullable
  @Override
  public <R> R mapDocumentNullable(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper) {
    return null;
  }

  @NotNull
  @Override
  public List<Document> getDocumentList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public <T> List<T> getSerializableList(@NotNull String path, @NotNull Class<T> classOfT) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public Document set(@NotNull String path, @Nullable Object value) {
    throw new ConfigReadOnlyException("set");
  }

  @NotNull
  @Override
  public Document set(@NotNull Object value) {
    throw new ConfigReadOnlyException("set");
  }

  @NotNull
  @Override
  public Document clear() {
    return this;
  }

  @NotNull
  @Override
  public Document remove(@NotNull String path) {
    return this;
  }

  @Override
  public void write(@NotNull Writer writer) throws IOException {
    throw new UnsupportedOperationException("EmptyDocument.write(Writer)");
  }

  @Override
  public void saveToFile(@NotNull File file) throws IOException {
    throw new UnsupportedOperationException("EmptyDocument.save(File)");
  }

  @Nullable
  @Override
  public Object getObject(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public Object getObject(@NotNull String path, @NotNull Object def) {
    return def;
  }

  @Override
  public <T> T getInstance(@NotNull String path, @NotNull Class<T> classOfT) {
    return null;
  }

  @Override
  public <T> T toInstanceOf(@NotNull Class<T> classOfT) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public <T, O extends Propertyable> Optional<T> getOptional(@NotNull String key, @NotNull BiFunction<O, ? super String, ? extends T> extractor) {
    return Optional.empty();
  }

  @Nullable
  @Override
  public String getString(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public String getString(@NotNull String path, @NotNull String def) {
    return def;
  }

  @Override
  public char getChar(@NotNull String path) {
    return 0;
  }

  @Override
  public char getChar(@NotNull String path, char def) {
    return def;
  }

  @Override
  public long getLong(@NotNull String path) {
    return 0;
  }

  @Override
  public long getLong(@NotNull String path, long def) {
    return def;
  }

  @Override
  public int getInt(@NotNull String path) {
    return 0;
  }

  @Override
  public int getInt(@NotNull String path, int def) {
    return def;
  }

  @Override
  public short getShort(@NotNull String path) {
    return 0;
  }

  @Override
  public short getShort(@NotNull String path, short def) {
    return def;
  }

  @Override
  public byte getByte(@NotNull String path) {
    return 0;
  }

  @Override
  public byte getByte(@NotNull String path, byte def) {
    return def;
  }

  @Override
  public float getFloat(@NotNull String path) {
    return 0;
  }

  @Override
  public float getFloat(@NotNull String path, float def) {
    return def;
  }

  @Override
  public double getDouble(@NotNull String path) {
    return 0;
  }

  @Override
  public double getDouble(@NotNull String path, double def) {
    return def;
  }

  @Override
  public boolean getBoolean(@NotNull String path) {
    return false;
  }

  @Override
  public boolean getBoolean(@NotNull String path, boolean def) {
    return def;
  }

  @Nullable
  @Override
  public byte[] getBinary(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public List<String> getStringList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public String[] getStringArray(@NotNull String path) {
    return new String[0];
  }

  @NotNull
  @Override
  public <E extends Enum<E>> List<E> getEnumList(@NotNull String path, @NotNull Class<E> classOfEnum) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public <T> List<T> mapList(@NotNull String path, @NotNull Function<String, ? extends T> mapper) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<UUID> getUUIDList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<Character> getCharacterList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<Byte> getByteList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<Short> getShortList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<Integer> getIntegerList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<Long> getLongList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<Float> getFloatList(@NotNull String path) {
    return new ArrayList<>();
  }

  @NotNull
  @Override
  public List<Double> getDoubleList(@NotNull String path) {
    return new ArrayList<>();
  }

  @Nullable
  @Override
  public UUID getUUID(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public UUID getUUID(@NotNull String path, @NotNull UUID def) {
    return def;
  }

  @Nullable
  @Override
  public Date getDate(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public Date getDate(@NotNull String path, @NotNull Date def) {
    return def;
  }

  @Nullable
  @Override
  public OffsetDateTime getDateTime(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public OffsetDateTime getDateTime(@NotNull String path, @NotNull OffsetDateTime def) {
    return def;
  }

  @Nullable
  @Override
  public Color getColor(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public Color getColor(@NotNull String path, @NotNull Color def) {
    return def;
  }

  @Nullable
  @Override
  public <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull Class<E> classOfEnum) {
    return null;
  }

  @NotNull
  @Override
  public <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull E def) {
    return def;
  }

  @Nullable
  @Override
  public <T> T getSerializable(@NotNull String path, @NotNull Class<T> classOfT) {
    return null;
  }

  @NotNull
  @Override
  public <T> T getSerializable(@NotNull String path, @NotNull T def) {
    return def;
  }

  @Nullable
  @Override
  public Class<?> getClass(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public Class<?> getClass(@NotNull String path, @NotNull Class<?> def) {
    return def;
  }

  @Nullable
  @Override
  public Version getVersion(@NotNull String path) {
    return null;
  }

  @NotNull
  @Override
  public Version getVersion(@NotNull String path, @NotNull Version def) {
    return def;
  }

  @Override
  public boolean contains(@NotNull String path) {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean hasChildren(@NotNull String path) {
    return false;
  }

  @Override
  public boolean isList(@NotNull String path) {
    return false;
  }

  @Override
  public boolean isObject(@NotNull String path) {
    return false;
  }

  @Override
  public boolean isDocument(@NotNull String path) {
    return false;
  }

  @Override
  public int size() {
    return 0;
  }

  @NotNull
  @Override
  public Map<String, Object> values() {
    return Collections.emptyMap();
  }

  @NotNull
  @Override
  public Map<String, String> valuesAsStrings() {
    return new HashMap<>();
  }

  @NotNull
  @Override
  public Map<String, Document> children() {
    return new HashMap<>();
  }

  @NotNull
  @Override
  public <K, V> Map<K, V> mapValues(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super String, ? extends V> valueMapper) {
    return new HashMap<>();
  }

  @NotNull
  @Override
  public <K, V> Map<K, V> mapDocuments(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super Document, ? extends V> valueMapper) {
    return new HashMap<>();
  }

  @NotNull
  @Override
  public Collection<String> keys() {
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public Set<String> deepKeys() {
    return Collections.emptySet();
  }

  @NotNull
  @Override
  public Set<Entry<String, Object>> entrySet() {
    return Collections.emptySet();
  }

  @Override
  public void forEach(@NotNull BiConsumer<? super String, ? super Object> action) {
  }

  @NotNull
  @Override
  public String toJson() {
    return "{}";
  }

  @NotNull
  @Override
  public String toPrettyJson() {
    return "{}";
  }

  @NotNull
  @Override
  public String toString() {
    return "{}";
  }

  @Override
  public boolean isReadonly() {
    return true;
  }

  @NotNull
  @Override
  public Document readonly() {
    return this;
  }

  @Nullable
  @Override
  public Document getParent() {
    return parent;
  }

  @NotNull
  @Override
  public Document getRoot() {
    return root;
  }

}
