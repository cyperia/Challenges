package net.cyperia.commons.common.config.document.wrapper;


import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.Propertyable;
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

public interface WrappedDocument<D extends Document> extends Document {

  Document getWrappedDocument();

  @Override
  default boolean isReadonly() {
    return getWrappedDocument().isReadonly();
  }

  @NotNull
  @Override
  default Document getDocument(@NotNull String path) {
    return getWrappedDocument().getDocument(path);
  }

  @NotNull
  @Override
  default List<Document> getDocumentList(@NotNull String path) {
    return getWrappedDocument().getDocumentList(path);
  }

  @NotNull
  @Override
  default <T> List<T> getSerializableList(@NotNull String path, @NotNull Class<T> classOfT) {
    return getWrappedDocument().getSerializableList(path, classOfT);
  }

  @NotNull
  @Override
  default D set(@NotNull String path, @Nullable Object value) {
    getWrappedDocument().set(path, value);
    return self();
  }

  @NotNull
  @Override
  default D set(@NotNull Object value) {
    getWrappedDocument().set(value);
    return self();
  }

  @NotNull
  @Override
  default D clear() {
    getWrappedDocument().clear();
    return self();
  }

  @NotNull
  @Override
  default D remove(@NotNull String path) {
    getWrappedDocument().remove(path);
    return self();
  }

  @Override
  default void write(@NotNull Writer writer) throws IOException {
    getWrappedDocument().write(writer);
  }

  @Override
  default void saveToFile(@NotNull File file) throws IOException {
    getWrappedDocument().saveToFile(file);
  }

  @NotNull
  @Override
  default String toJson() {
    return getWrappedDocument().toJson();
  }

  @NotNull
  @Override
  default String toPrettyJson() {
    return getWrappedDocument().toPrettyJson();
  }

  @Override
  default <T> T getInstance(@NotNull String path, @NotNull Class<T> classOfT) {
    return getWrappedDocument().getInstance(path, classOfT);
  }

  @Override
  default <T> T toInstanceOf(@NotNull Class<T> classOfT) {
    return getWrappedDocument().toInstanceOf(classOfT);
  }

  @Nullable
  @Override
  default Object getObject(@NotNull String path) {
    return getWrappedDocument().getObject(path);
  }

  @NotNull
  @Override
  default Object getObject(@NotNull String path, @NotNull Object def) {
    return getWrappedDocument().getObject(path, def);
  }

  @NotNull
  @Override
  default <T, O extends Propertyable> Optional<T> getOptional(@NotNull String key, @NotNull BiFunction<O, ? super String, ? extends T> extractor) {
    return getWrappedDocument().getOptional(key, extractor);
  }

  @Nullable
  @Override
  default String getString(@NotNull String path) {
    return getWrappedDocument().getString(path);
  }

  @NotNull
  @Override
  default String getString(@NotNull String path, @NotNull String def) {
    return getWrappedDocument().getString(path, def);
  }

  @Override
  default char getChar(@NotNull String path) {
    return getWrappedDocument().getChar(path);
  }

  @Override
  default char getChar(@NotNull String path, char def) {
    return getWrappedDocument().getChar(path, def);
  }

  @Override
  default long getLong(@NotNull String path) {
    return getWrappedDocument().getLong(path);
  }

  @Override
  default long getLong(@NotNull String path, long def) {
    return getWrappedDocument().getLong(path, def);
  }

  @Override
  default int getInt(@NotNull String path) {
    return getWrappedDocument().getInt(path);
  }

  @Override
  default int getInt(@NotNull String path, int def) {
    return getWrappedDocument().getInt(path, def);
  }

  @Override
  default short getShort(@NotNull String path) {
    return getWrappedDocument().getShort(path);
  }

  @Override
  default short getShort(@NotNull String path, short def) {
    return getWrappedDocument().getShort(path, def);
  }

  @Override
  default byte getByte(@NotNull String path) {
    return getWrappedDocument().getByte(path);
  }

  @Override
  default byte getByte(@NotNull String path, byte def) {
    return getWrappedDocument().getByte(path, def);
  }

  @Override
  default float getFloat(@NotNull String path) {
    return getWrappedDocument().getFloat(path);
  }

  @Override
  default float getFloat(@NotNull String path, float def) {
    return getWrappedDocument().getFloat(path, def);
  }

  @Override
  default double getDouble(@NotNull String path) {
    return getWrappedDocument().getDouble(path);
  }

  @Override
  default double getDouble(@NotNull String path, double def) {
    return getWrappedDocument().getDouble(path, def);
  }

  @Override
  default boolean getBoolean(@NotNull String path) {
    return getWrappedDocument().getBoolean(path);
  }

  @Override
  default boolean getBoolean(@NotNull String path, boolean def) {
    return getWrappedDocument().getBoolean(path, def);
  }

  @NotNull
  @Override
  default List<String> getStringList(@NotNull String path) {
    return getWrappedDocument().getStringList(path);
  }

  @NotNull
  @Override
  default String[] getStringArray(@NotNull String path) {
    return getWrappedDocument().getStringArray(path);
  }

  @NotNull
  @Override
  default <T> List<T> mapList(@NotNull String path, @NotNull Function<String, ? extends T> mapper) {
    return getWrappedDocument().mapList(path, mapper);
  }

  @NotNull
  @Override
  default <E extends Enum<E>> List<E> getEnumList(@NotNull String path, @NotNull Class<E> classOfEnum) {
    return getWrappedDocument().getEnumList(path, classOfEnum);
  }

  @NotNull
  @Override
  default List<UUID> getUUIDList(@NotNull String path) {
    return getWrappedDocument().getUUIDList(path);
  }

  @NotNull
  @Override
  default List<Character> getCharacterList(@NotNull String path) {
    return getWrappedDocument().getCharacterList(path);
  }

  @NotNull
  @Override
  default List<Byte> getByteList(@NotNull String path) {
    return getWrappedDocument().getByteList(path);
  }

  @NotNull
  @Override
  default List<Short> getShortList(@NotNull String path) {
    return getWrappedDocument().getShortList(path);
  }

  @NotNull
  @Override
  default List<Integer> getIntegerList(@NotNull String path) {
    return getWrappedDocument().getIntegerList(path);
  }

  @NotNull
  @Override
  default List<Long> getLongList(@NotNull String path) {
    return getWrappedDocument().getLongList(path);
  }

  @NotNull
  @Override
  default List<Float> getFloatList(@NotNull String path) {
    return getWrappedDocument().getFloatList(path);
  }

  @NotNull
  @Override
  default List<Double> getDoubleList(@NotNull String path) {
    return getWrappedDocument().getDoubleList(path);
  }

  @Nullable
  @Override
  default UUID getUUID(@NotNull String path) {
    return getWrappedDocument().getUUID(path);
  }

  @NotNull
  @Override
  default UUID getUUID(@NotNull String path, @NotNull UUID def) {
    return getWrappedDocument().getUUID(path, def);
  }

  @Nullable
  @Override
  default Date getDate(@NotNull String path) {
    return getWrappedDocument().getDate(path);
  }

  @NotNull
  @Override
  default Date getDate(@NotNull String path, @NotNull Date def) {
    return getWrappedDocument().getDate(path, def);
  }

  @Nullable
  @Override
  default OffsetDateTime getDateTime(@NotNull String path) {
    return getWrappedDocument().getDateTime(path);
  }

  @NotNull
  @Override
  default OffsetDateTime getDateTime(@NotNull String path, @NotNull OffsetDateTime def) {
    return getWrappedDocument().getDateTime(path, def);
  }

  @Nullable
  @Override
  default Color getColor(@NotNull String path) {
    return getWrappedDocument().getColor(path);
  }

  @NotNull
  @Override
  default Color getColor(@NotNull String path, @NotNull Color def) {
    return getWrappedDocument().getColor(path, def);
  }

  @Nullable
  @Override
  default <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull Class<E> classOfEnum) {
    return getWrappedDocument().getEnum(path, classOfEnum);
  }

  @NotNull
  @Override
  default <E extends Enum<E>> E getEnum(@NotNull String path, @NotNull E def) {
    return getWrappedDocument().getEnum(path, def);
  }

  @Nullable
  @Override
  default <T> T getSerializable(@NotNull String path, @NotNull Class<T> classOfT) {
    return getWrappedDocument().getSerializable(path, classOfT);
  }

  @NotNull
  @Override
  default <T> T getSerializable(@NotNull String path, @NotNull T def) {
    return getWrappedDocument().getSerializable(path, def);
  }

  @Nullable
  @Override
  default Class<?> getClass(@NotNull String path) {
    return getWrappedDocument().getClass(path);
  }

  @NotNull
  @Override
  default Class<?> getClass(@NotNull String path, @NotNull Class<?> def) {
    return getWrappedDocument().getClass(path, def);
  }

  @Nullable
  @Override
  default Version getVersion(@NotNull String path) {
    return getWrappedDocument().getVersion(path);
  }

  @NotNull
  @Override
  default Version getVersion(@NotNull String path, @NotNull Version def) {
    return getWrappedDocument().getVersion(path, def);
  }

  @Nullable
  @Override
  default byte[] getBinary(@NotNull String path) {
    return getWrappedDocument().getBinary(path);
  }

  @Override
  default boolean contains(@NotNull String path) {
    return getWrappedDocument().contains(path);
  }

  @Override
  default boolean hasChildren(@NotNull String path) {
    return getWrappedDocument().hasChildren(path);
  }

  @Override
  default boolean isObject(@NotNull String path) {
    return getWrappedDocument().isObject(path);
  }

  @Override
  default boolean isList(@NotNull String path) {
    return getWrappedDocument().isList(path);
  }

  @Override
  default boolean isDocument(@NotNull String path) {
    return getWrappedDocument().isDocument(path);
  }

  @Override
  default boolean isEmpty() {
    return getWrappedDocument().isEmpty();
  }

  @Override
  default int size() {
    return getWrappedDocument().size();
  }

  @NotNull
  @Override
  default Map<String, Object> values() {
    return getWrappedDocument().values();
  }

  @NotNull
  @Override
  default Map<String, String> valuesAsStrings() {
    return getWrappedDocument().valuesAsStrings();
  }

  @NotNull
  @Override
  default Map<String, Document> children() {
    return getWrappedDocument().children();
  }

  @NotNull
  @Override
  default <K, V> Map<K, V> mapValues(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super String, ? extends V> valueMapper) {
    return getWrappedDocument().mapValues(keyMapper, valueMapper);
  }

  @NotNull
  @Override
  default <K, V> Map<K, V> mapDocuments(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super Document, ? extends V> valueMapper) {
    return getWrappedDocument().mapDocuments(keyMapper, valueMapper);
  }

  @NotNull
  @Override
  default <R> R mapDocument(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper) {
    return getWrappedDocument().mapDocument(path, mapper);
  }

  @Nullable
  @Override
  default <R> R mapDocumentNullable(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper) {
    return getWrappedDocument().mapDocumentNullable(path, mapper);
  }

  @NotNull
  @Override
  default Collection<String> keys() {
    return getWrappedDocument().keys();
  }

  @NotNull
  @Override
  default Set<String> deepKeys() {
    return getWrappedDocument().deepKeys();
  }

  @NotNull
  @Override
  default Set<Entry<String, Object>> entrySet() {
    return getWrappedDocument().entrySet();
  }

  @Override
  default void forEach(@NotNull BiConsumer<? super String, ? super Object> action) {
    getWrappedDocument().forEach(action);
  }

  @NotNull
  @Override
  default Document readonly() {
    return getWrappedDocument().readonly();
  }

  @Nullable
  @Override
  default Document getParent() {
    return getWrappedDocument().getParent();
  }

  @NotNull
  @Override
  default Document getRoot() {
    return getWrappedDocument().getRoot();
  }

  @SuppressWarnings("unchecked")
  default D self() {
    return (D) this;
  }

}
