package net.cyperia.commons.common.config.document.gson;

import com.google.gson.Gson;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ClassTypeAdapter implements GsonTypeAdapter<Class<?>> {

  @Override
  public void write(@NotNull Gson gson, @NotNull JsonWriter writer, @NotNull Class<?> object) throws IOException {
    TypeAdapters.STRING.write(writer, object.getName());
  }

  @Override
  public Class<?> read(@NotNull Gson gson, @NotNull JsonReader reader) throws IOException {
    try {

      String value = reader.nextString();
      if (value == null) return null;

      return Class.forName(value);

    } catch (ClassNotFoundException ex) {
      return null;
    }
  }

}
