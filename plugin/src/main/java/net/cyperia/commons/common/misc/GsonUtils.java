package net.cyperia.commons.common.misc;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public final class GsonUtils {

  private GsonUtils() {
  }

  @Nullable
  public static Object unpackJsonElement(@Nullable JsonElement element) {
    if (element == null || element.isJsonNull())
      return null;
    if (element.isJsonObject())
      return convertJsonObjectToMap(element.getAsJsonObject());
    if (element.isJsonArray())
      return convertJsonArrayToStringList(element.getAsJsonArray());
    if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isNumber()) return primitive.getAsNumber();
      if (primitive.isString()) return primitive.getAsString();
      if (primitive.isBoolean()) return primitive.getAsBoolean();
    }
    return element;
  }

  @Nullable
  public static String convertJsonElementToString(@Nullable JsonElement element) {
    if (element == null || element.isJsonNull())
      return null;
    if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isString()) return primitive.getAsString();
      if (primitive.isNumber()) return primitive.getAsNumber() + "";
      if (primitive.isBoolean()) return primitive.getAsBoolean() + "";
    }
    return element.toString();
  }

  @NotNull
  public static Map<String, Object> convertJsonObjectToMap(@NotNull JsonObject object) {
    Map<String, Object> map = new LinkedHashMap<>();
    convertJsonObjectToMap(object, map);
    return map;
  }

  public static void convertJsonObjectToMap(@NotNull JsonObject object, @NotNull Map<String, Object> map) {
    for (Entry<String, JsonElement> entry : object.entrySet()) {
      map.put(entry.getKey(), unpackJsonElement(entry.getValue()));
    }
  }

  @NotNull
  public static List<String> convertJsonArrayToStringList(@NotNull JsonArray array) {
    List<String> list = new ArrayList<>(array.size());
    for (JsonElement element : array) {
      list.add(convertJsonElementToString(element));
    }
    return list;
  }

  @NotNull
  public static String[] convertJsonArrayToStringArray(@NotNull JsonArray array) {
    String[] list = new String[array.size()];
    for (int i = 0; i < array.size(); i++) {
      list[i] = convertJsonElementToString(array.get(i));
    }
    return list;
  }

  @NotNull
  public static JsonArray convertIterableToJsonArray(@NotNull Gson gson, @NotNull Iterable<?> iterable) {
    JsonArray array = new JsonArray();
    iterable.forEach(object -> array.add(gson.toJsonTree(object)));
    return array;
  }

  @NotNull
  public static JsonArray convertArrayToJsonArray(@NotNull Gson gson, @NotNull Object array) {
    JsonArray jsonArray = new JsonArray();
    ReflectionUtils.forEachInArray(array, object -> jsonArray.add(gson.toJsonTree(object)));
    return jsonArray;
  }

  public static void setDocumentProperties(@NotNull Gson gson, @NotNull JsonObject object, @NotNull Map<String, Object> values) {
    for (Entry<String, Object> entry : values.entrySet()) {
      Object value = entry.getValue();

      if (value == null) {
        object.add(entry.getKey(), null);
      } else if (value instanceof JsonElement) {
        object.add(entry.getKey(), (JsonElement) value);
      } else if (value instanceof Iterable) {
        Iterable<?> iterable = (Iterable<?>) value;
        object.add(entry.getKey(), convertIterableToJsonArray(gson, iterable));
      } else if (value.getClass().isArray()) {
        object.add(entry.getKey(), convertArrayToJsonArray(gson, value));
      } else if (value instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) value;
        JsonObject newObject = new JsonObject();
        object.add(entry.getKey(), newObject);
        setDocumentProperties(gson, newObject, map);
      } else {
        object.add(entry.getKey(), gson.toJsonTree(value));
      }
    }
  }

}
