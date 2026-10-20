package net.cyperia.challenges.catalog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Locales {

  public static final String GLOBAL = "global";

  private Locales() {
  }

  /** The bundled messages of {@code language} over {@code global} by dot path, resolved except for positional arguments. */
  @NotNull
  public static Map<String, String[]> load(@NotNull String language) {
    Map<String, String[]> bundle = new HashMap<>();
    flattenInto(read(GLOBAL), bundle, "");
    flattenInto(read(language), bundle, "");
    return resolve(bundle);
  }

  /** Resolves references and colorizers; meta tags such as {@code *colorize*} are left out. */
  @NotNull
  public static Map<String, String[]> resolve(@NotNull Map<String, String[]> bundle) {
    Map<String, String[]> resolved = new HashMap<>(bundle.size());
    bundle.forEach((key, value) -> {
      if (!MessageFormatter.isMetaTag(key)) {
        resolved.put(key, MessageFormatter.embedReferences(value, key, bundle));
      }
    });
    return resolved;
  }

  private static void flattenInto(@NotNull JsonObject from, @NotNull Map<String, String[]> into, @NotNull String path) {
    for (Map.Entry<String, JsonElement> entry : from.entrySet()) {
      String key = path + entry.getKey();
      JsonElement value = entry.getValue();
      if (value.isJsonObject()) {
        flattenInto(value.getAsJsonObject(), into, key + MessageFormatter.SUB_DOCUMENT_DELIMITER);
      } else if (value.isJsonArray()) {
        into.put(key, value.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toArray(String[]::new));
      } else {
        into.put(key, new String[]{value.getAsString()});
      }
    }
  }

  @NotNull
  private static JsonObject read(@NotNull String language) {
    String path = "language/locales/" + language + ".json";
    try (InputStream in = Locales.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) throw new IllegalArgumentException("No bundled language " + language);
      return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

}
