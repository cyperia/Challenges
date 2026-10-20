package net.cyperia.commons.common.config.document.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.GsonDocument;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DocumentTypeAdapter implements GsonTypeAdapter<Document> {

  @Override
  public void write(@NotNull Gson gson, @NotNull JsonWriter writer, @NotNull Document document) throws IOException {
    if (document instanceof GsonDocument) {
      GsonDocument gsonDocument = (GsonDocument) document;
      TypeAdapters.JSON_ELEMENT.write(writer, gsonDocument.getJsonObject());
      return;
    }

    Document copiedDocument = document.copyJson();
    if (copiedDocument instanceof GsonDocument) {
      GsonDocument gsonDocument = (GsonDocument) copiedDocument;
      TypeAdapters.JSON_ELEMENT.write(writer, gsonDocument.getJsonObject());
      return;
    }

    GsonDocument gsonDocument = new GsonDocument(document.values());
    TypeAdapters.JSON_ELEMENT.write(writer, gsonDocument.getJsonObject());

  }

  @Override
  public Document read(@NotNull Gson gson, @NotNull JsonReader reader) throws IOException {
    JsonElement jsonElement = TypeAdapters.JSON_ELEMENT.read(reader);
    if (jsonElement != null && jsonElement.isJsonObject()) {
      return new GsonDocument(jsonElement.getAsJsonObject());
    } else {
      return null;
    }
  }

}
