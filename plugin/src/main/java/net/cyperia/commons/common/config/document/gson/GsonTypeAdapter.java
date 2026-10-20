package net.cyperia.commons.common.config.document.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public interface GsonTypeAdapter<T> {

  void write(@NotNull Gson gson, @NotNull JsonWriter writer, @NotNull T object) throws IOException;

  T read(@NotNull Gson gson, @NotNull JsonReader reader) throws IOException;

  default TypeAdapter<T> toTypeAdapter(@NotNull Gson gson) {
    return new TypeAdapter<T>() {
      @Override
      public void write(JsonWriter writer, T object) throws IOException {
        if (object == null) {
          writer.nullValue();
          return;
        }
        GsonTypeAdapter.this.write(gson, writer, object);
      }

      @Override
      public T read(JsonReader reader) throws IOException {
        return GsonTypeAdapter.this.read(gson, reader);
      }
    };
  }

  @NotNull
  static TypeAdapterFactory newTypeHierarchyFactory(@NotNull Class<?> clazz, @NotNull GsonTypeAdapter<?> adapter) {
    return new TypeAdapterFactory() {
      @Override
      public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> token) {
        Class<? super R> requestedType = token.getRawType();
        if (!clazz.isAssignableFrom(requestedType)) return null;

        return (TypeAdapter<R>) adapter.toTypeAdapter(gson);
      }
    };
  }

  @NotNull
  static TypeAdapterFactory newPredictableFactory(@NotNull Predicate<Class<?>> predicate, @NotNull GsonTypeAdapter<?> adapter) {
    return new TypeAdapterFactory() {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return predicate.test(type.getRawType()) ? (TypeAdapter<T>) adapter.toTypeAdapter(gson) : null;
      }
    };
  }

}
