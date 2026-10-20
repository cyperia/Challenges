package net.cyperia.commons.common.config;

import com.google.gson.JsonArray;
import net.cyperia.commons.common.collection.WrappedException;
import net.cyperia.commons.common.config.document.EmptyDocument;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.cyperia.commons.common.config.document.PropertiesDocument;
import net.cyperia.commons.common.misc.FileUtils;
import net.cyperia.commons.common.misc.GsonUtils;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Document extends Config, Json {

  /**
   * Gets the document located at the given path.
   * If there is no document assigned to this path,
   * a new document is being created and assigned to this path.
   *
   * @return the document assigned to this path
   */
  @NotNull
  Document getDocument(@NotNull String path);

  /**
   * Returns the list of documents located at the given path.
   * The returned list will not contain any null elements.
   * If there are no documents, the list will be empty.
   * Elements which are no documents in the original list will be skipped.
   * If an element is {@code null} an empty document will be added
   * if this document is not {@link #isReadonly() readonly}.
   *
   * @return the list of documents assigned to this path
   */
  @NotNull
  List<Document> getDocumentList(@NotNull String path);

  @NotNull
  default <T> List<T> getInstanceList(@NotNull String path, @NotNull Class<T> classOfT) {
    List<Document> documents = getDocumentList(path);
    List<T> result = new ArrayList<>(documents.size());
    for (Document document : documents) {
      result.add(document.toInstanceOf(classOfT));
    }
    return result;
  }

  @NotNull
  <T> List<T> getSerializableList(@NotNull String path, @NotNull Class<T> classOfT);

  @Nullable
  <T> T getSerializable(@NotNull String path, @NotNull Class<T> classOfT);

  @NotNull
  <T> T getSerializable(@NotNull String path, @NotNull T def);

  @NotNull
  <K, V> Map<K, V> mapDocuments(@NotNull Function<? super String, ? extends K> keyMapper, @NotNull Function<? super Document, ? extends V> valueMapper);

  @NotNull
  <R> R mapDocument(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper);

  @Nullable
  <R> R mapDocumentNullable(@NotNull String path, @NotNull Function<? super Document, ? extends R> mapper);

  @Nullable
  <T> T toInstanceOf(@NotNull Class<T> classOfT);

  /**
   * Returns the parent document of this document.
   * If {@code this} is the {@link #getRoot() root} this method will return {@code null}.
   *
   * @return the parent document of this document
   */
  @Nullable
  Document getParent();

  /**
   * Returns the root document of this document.
   * If the {@link #getParent() parent} is {@code null} this will return {@code this}.
   *
   * @return the root document of this document
   */
  @NotNull
  Document getRoot();

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  Document set(@NotNull String path, @Nullable Object value);

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  Document clear();

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  Document remove(@NotNull String path);

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  default <O extends Propertyable> Document apply(@NotNull Consumer<O> action) {
    return (Document) Config.super.apply(action);
  }

  @NotNull
  @Override
  default <O extends Propertyable> Document applyIf(boolean expression, @NotNull Consumer<O> action) {
    return (Document) Config.super.applyIf(expression, action);
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  default Document setIfAbsent(@NotNull String path, @NotNull Object defaultValue) {
    return (Document) Config.super.setIfAbsent(path, defaultValue);
  }

  @NotNull
  Document set(@NotNull Object value);

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  @CheckReturnValue
  Document readonly();

  @NotNull
  Map<String, Document> children();

  @NotNull
  Set<String> deepKeys();

  boolean isDocument(@NotNull String path);

  boolean hasChildren(@NotNull String path);

  void write(@NotNull Writer writer) throws IOException;

  default void saveToFile(@NotNull File file) throws IOException {
    FileUtils.createFilesIfNecessary(file);
    Writer writer = FileUtils.newBufferedWriter(file);
    write(writer);
    writer.flush();
    writer.close();
  }

  default void saveToFile(@NotNull Path file) throws IOException {
    FileUtils.createFile(file);
    Writer writer = FileUtils.newBufferedWriter(file);
    write(writer);
    writer.flush();
    writer.close();
  }

  @NotNull
  @CheckReturnValue
  default FileDocument asFileDocument(@NotNull File file) {
    return (this instanceof FileDocument && ((FileDocument) this).getFile().equals(file))
      ? (FileDocument) this : FileDocument.wrap(this, file);
  }

  @NotNull
  @CheckReturnValue
  default FileDocument asFileDocument(@NotNull Path file) {
    return asFileDocument(file.toFile());
  }

  @NotNull
  @CheckReturnValue
  default Document copyJson() {
    Document document = create();
    this.forEach(document::set);
    return document;
  }

  /**
   * @return an empty and immutable document
   * @see EmptyDocument
   */
  @NotNull
  @CheckReturnValue
  static Document empty() {
    return EmptyDocument.ROOT;
  }

  @NotNull
  @CheckReturnValue
  static Document readFile(@NotNull Class<? extends Document> classOfDocument, @NotNull File file) {
    try {
      if (file.exists()) {
        Constructor<? extends Document> constructor = classOfDocument.getConstructor(File.class);
        return constructor.newInstance(file);
      } else {
        Constructor<? extends Document> constructor = classOfDocument.getConstructor();
        return constructor.newInstance();
      }
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
      throw new UnsupportedOperationException(classOfDocument.getName() + " does not support File creation");
    } catch (InvocationTargetException ex) {
      throw new WrappedException(ex);
    }
  }

  @NotNull
  @CheckReturnValue
  static Document readFile(@NotNull Class<? extends Document> classOfDocument, @NotNull Path file) {
    return readFile(classOfDocument, file.toFile());
  }

  /**
   * @return a json document parsed by the input
   * @see GsonDocument
   */
  @NotNull
  @CheckReturnValue
  static Document parseJson(@NotNull String jsonInput) {
    return new GsonDocument(jsonInput);
  }

  @NotNull
  @CheckReturnValue
  static Document parseJson(@NotNull Reader reader) throws IOException {
    return new GsonDocument(reader);
  }

  @NotNull
  @CheckReturnValue
  static Document parseJson(@NotNull InputStream input) throws IOException {
    return new GsonDocument(new InputStreamReader(input, StandardCharsets.UTF_8));
  }

  @NotNull
  @CheckReturnValue
  static List<Document> parseJsonArray(@NotNull String jsonInput) {
    return GsonDocument.convertArrayToDocuments(GsonDocument.GSON.fromJson(jsonInput, JsonArray.class));
  }

  @NotNull
  @CheckReturnValue
  static List<String> parseJsonStringArray(@NotNull String jsonInput) {
    return GsonDocument.convertArrayToStrings(GsonDocument.GSON.fromJson(jsonInput, JsonArray.class));
  }

  static void saveArray(@NotNull Iterable<?> objects, @NotNull Path file) throws IOException {
    FileUtils.createFile(file);
    Writer writer = FileUtils.newBufferedWriter(file);
    JsonArray array = GsonUtils.convertIterableToJsonArray(GsonDocument.GSON, objects);
    (GsonDocument.isWritePrettyJson() ? GsonDocument.GSON_PRETTY_PRINT : GsonDocument.GSON).toJson(array, writer);
    writer.flush();
    writer.close();
  }

  @NotNull
  @CheckReturnValue
  static Document readJsonFile(@NotNull File file) {
    return readFile(GsonDocument.class, file);
  }

  @NotNull
  @CheckReturnValue
  static Document readJsonFile(@NotNull Path file) {
    return readJsonFile(file.toFile());
  }

  @NotNull
  @CheckReturnValue
  static List<Document> readJsonArrayFile(@NotNull Path file) {
    try {
      JsonArray array = GsonDocument.GSON.fromJson(FileUtils.newBufferedReader(file), JsonArray.class);
      if (array == null) return new ArrayList<>();
      List<Document> documents = new ArrayList<>(array.size());
      array.forEach(element -> documents.add(new GsonDocument(element.getAsJsonObject())));
      return documents;
    } catch (IOException ex) {
      throw new WrappedException(ex);
    }
  }

  @NotNull
  @CheckReturnValue
  static Document readPropertiesFile(@NotNull File file) {
    return readFile(PropertiesDocument.class, file);
  }

  @NotNull
  @CheckReturnValue
  static Document readPropertiesFile(@NotNull Path file) {
    return readPropertiesFile(file.toFile());
  }

  @NotNull
  @CheckReturnValue
  static Document create() {
    return new GsonDocument();
  }

  @NotNull
  @CheckReturnValue
  static Document of(@NotNull Object object) {
    return new GsonDocument(object);
  }

  @Nullable
  @CheckReturnValue
  static Document ofNullable(@Nullable Object object) {
    return object == null ? null : of(object);
  }

  @NotNull
  @CheckReturnValue
  static List<Document> arrayOf(@NotNull Collection<?> objects) {
    List<Document> documents = new ArrayList<>(objects.size());
    objects.forEach(object -> documents.add(Document.of(object)));
    return documents;
  }

}
