package net.cyperia.commons.common.config.document.readonly;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.wrapper.WrappedDocument;
import org.jetbrains.annotations.NotNull;

public final class ReadOnlyDocumentWrapper implements WrappedDocument<Document> {

  private final Document document;

  public ReadOnlyDocumentWrapper(@NotNull Document document) {
    this.document = document;
  }

  @Override
  public Document getWrappedDocument() {
    return document;
  }

  @Override
  public boolean isReadonly() {
    return true;
  }

}
