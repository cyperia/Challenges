package net.cyperia.commons.common.config.document.gson;

import com.google.gson.Gson;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.cyperia.commons.common.collection.Colors;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;

public class ColorTypeAdapter implements GsonTypeAdapter<Color> {

  @Override
  public void write(@NotNull Gson gson, @NotNull JsonWriter writer, @NotNull Color color) throws IOException {
    TypeAdapters.STRING.write(writer, Colors.asHex(color));
  }

  @Override
  public Color read(@NotNull Gson gson, @NotNull JsonReader reader) throws IOException {
    String value = TypeAdapters.STRING.read(reader);
    if (value == null) return null;
    return Color.decode(value);
  }

}
