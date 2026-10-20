package net.cyperia.commons.common.config.document.gson;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.cyperia.commons.common.collection.pair.Pair;
import net.cyperia.commons.common.collection.pair.Quadro;
import net.cyperia.commons.common.collection.pair.Triple;
import net.cyperia.commons.common.collection.pair.Tuple;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PairTypeAdapter implements GsonTypeAdapter<Pair> {

  @Override
  public void write(@NotNull Gson gson, @NotNull JsonWriter writer, @NotNull Pair object) throws IOException {
    Object[] values = object.values();
    JsonArray array = new JsonArray(values.length); // TODO fix(deps) version ambiguity
    for (Object value : values) {
      array.add(gson.toJsonTree(value));
    }
  }

  @Override
  public Pair read(@NotNull Gson gson, @NotNull JsonReader reader) throws IOException {
    JsonArray array = gson.fromJson(reader, JsonArray.class);
    int size = array.size();
    switch (size) {
      case 2:
        return Tuple.of(array.get(0), array.get(1));
      case 3:
        return Triple.of(array.get(0), array.get(1), array.get(2));
      case 4:
        return Quadro.of(array.get(0), array.get(1), array.get(2), array.get(3));
      default:
        throw new IllegalStateException("No Pair known for amount of " + size);
    }
  }
}
