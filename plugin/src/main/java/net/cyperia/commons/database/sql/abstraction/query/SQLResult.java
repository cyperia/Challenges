package net.cyperia.commons.database.sql.abstraction.query;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.EmptyDocument;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.cyperia.commons.common.config.document.MapDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class SQLResult extends MapDocument {

  public SQLResult(@NotNull Map<String, Object> values) {
    super(values);
  }

  @NotNull
  @Override
  public Document getDocument0(@NotNull String path, @NotNull Document root, @Nullable Document parent) {
    try {
      return new GsonDocument(getString(path), this, this).readonly();
    } catch (Exception ex) {
      return new EmptyDocument(this, null);
    }
  }

  @Override
  public boolean isReadonly() {
    return true;
  }

}
