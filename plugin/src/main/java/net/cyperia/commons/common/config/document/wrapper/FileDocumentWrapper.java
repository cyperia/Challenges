package net.cyperia.commons.common.config.document.wrapper;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.FileDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

public class FileDocumentWrapper implements WrappedDocument<FileDocument>, FileDocument {

  protected final Document document;
  protected final File file;

  public FileDocumentWrapper(@NotNull File file, @NotNull Document document) {
    this.file = file;
    this.document = document;
  }

  @Override
  public Document getWrappedDocument() {
    return document;
  }

  @NotNull
  @Override
  public File getFile() {
    return file;
  }

  @NotNull
  @Override
  public Path getPath() {
    return file.toPath();
  }

  @NotNull
  @Override
  public FileDocument set(@NotNull String path, @Nullable Object value) {
    return WrappedDocument.super.set(path, value);
  }

  @NotNull
  @Override
  public FileDocument set(@NotNull Object value) {
    return WrappedDocument.super.set(value);
  }

  @NotNull
  @Override
  public FileDocument clear() {
    return WrappedDocument.super.clear();
  }

  @NotNull
  @Override
  public FileDocument remove(@NotNull String path) {
    return WrappedDocument.super.remove(path);
  }

}
