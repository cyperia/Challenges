package net.cyperia.commons.common.config;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public final class PropertyHelper {

  private PropertyHelper() {
  }

  private static final Map<Type, BiFunction<? extends Propertyable, ? super String, ?>> getters = new HashMap<>();

  static {
    getters.put(Character.class, Propertyable::getChar);
    getters.put(Boolean.class, Propertyable::getBoolean);
    getters.put(Byte.class, Propertyable::getByte);
    getters.put(Short.class, Propertyable::getShort);
    getters.put(Integer.class, Propertyable::getInt);
    getters.put(Long.class, Propertyable::getLong);
    getters.put(Float.class, Propertyable::getFloat);
    getters.put(Double.class, Propertyable::getDouble);
    getters.put(String.class, Propertyable::getString);
    getters.put(CharSequence.class, Propertyable::getString);
    getters.put(List.class, Propertyable::getStringList);
    getters.put(Document.class, (BiFunction<Document, String, Document>) Document::getDocument);
    getters.put(String[].class, Propertyable::getStringArray);
    getters.put(Date.class, Propertyable::getDate);
    getters.put(OffsetDateTime.class, Propertyable::getDateTime);
    getters.put(Color.class, Propertyable::getColor);
    getters.put(byte[].class, Propertyable::getBinary);
  }

  @Nullable
  public static Date parseDate(@Nullable String string) {
    try {
      return DateFormat.getDateTimeInstance().parse(string);
    } catch (Exception ex) {
      return null;
    }
  }

}
