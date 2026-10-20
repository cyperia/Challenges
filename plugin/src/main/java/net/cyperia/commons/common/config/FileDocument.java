package net.cyperia.commons.common.config;

import net.cyperia.commons.common.concurrent.task.Task;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.cyperia.commons.common.config.document.PropertiesDocument;
import net.cyperia.commons.common.config.document.wrapper.FileDocumentWrapper;
import net.cyperia.commons.common.logging.ILogger;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface FileDocument extends Document {

  /**
   * Logger used to log errors which are caught in the safe version of an exceptionally method
   */
  ILogger LOGGER = ILogger.forThisClass();

  default void saveExceptionally() throws IOException {
    saveToFile(getFile());
  }

  /**
   * Executes {@link #saveExceptionally()} and prints all caught errors to {@link #LOGGER}
   *
   * @see #saveExceptionally()
   */
  default void save() {
    try {
      saveExceptionally();
    } catch (IOException ex) {
      LOGGER.error("Could not save config to file \"{}\"", getFile(), ex);
    }
  }

  @NotNull
  default Task<Void> saveAsync() {
    return Task.asyncRunExceptionally(this::save);
  }

  /**
   * @param async whether this should be saved asynchronously or synchronously
   * @see #save()
   * @see #saveAsync()
   */
  default void save(boolean async) {
    if (async) saveAsync();
    else save();
  }

  @NotNull
  File getFile();

  @NotNull
  Path getPath();

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  FileDocument set(@NotNull String path, @Nullable Object value);

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  FileDocument set(@NotNull Object value);

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  FileDocument clear();

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  FileDocument remove(@NotNull String path);

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  default <O extends Propertyable> FileDocument apply(@NotNull Consumer<O> action) {
    return (FileDocument) Document.super.apply(action);
  }

  @NotNull
  @Override
  default FileDocument setIfAbsent(@NotNull String path, @NotNull Object defaultValue) {
    return (FileDocument) Document.super.setIfAbsent(path, defaultValue);
  }

  @NotNull
  @Override
  default FileDocument increment(@NotNull String path, double amount) {
    return (FileDocument) Document.super.increment(path, amount);
  }

  @NotNull
  @Override
  default FileDocument decrement(@NotNull String path, double amount) {
    return (FileDocument) Document.super.decrement(path, amount);
  }

  @NotNull
  @Override
  default FileDocument multiply(@NotNull String path, double factor) {
    return (FileDocument) Document.super.multiply(path, factor);
  }

  @NotNull
  @Override
  default FileDocument divide(@NotNull String path, double divisor) {
    return (FileDocument) Document.super.divide(path, divisor);
  }

  @NotNull
  @CheckReturnValue
  static FileDocument wrap(@NotNull Document document, @NotNull File file) {
    return new FileDocumentWrapper(file, document);
  }

  @NotNull
  @CheckReturnValue
  static FileDocument readFile(@NotNull Class<? extends Document> classOfDocument, @NotNull File file) {
    return Document.readFile(classOfDocument, file).asFileDocument(file);
  }

  @NotNull
  @CheckReturnValue
  static FileDocument readFile(@NotNull Class<? extends Document> classOfDocument, @NotNull Path file) {
    return Document.readFile(classOfDocument, file).asFileDocument(file);
  }

  @NotNull
  @CheckReturnValue
  static FileDocument readJsonFile(@NotNull File file) {
    return readFile(GsonDocument.class, file);
  }

  @NotNull
  @CheckReturnValue
  static FileDocument readJsonFile(@NotNull Path file) {
    return readFile(GsonDocument.class, file);
  }

  @NotNull
  @CheckReturnValue
  static FileDocument readPropertiesFile(@NotNull File file) {
    return readFile(PropertiesDocument.class, file);
  }

  @NotNull
  @CheckReturnValue
  static FileDocument readPropertiesFile(@NotNull Path file) {
    return readFile(PropertiesDocument.class, file);
  }

}
