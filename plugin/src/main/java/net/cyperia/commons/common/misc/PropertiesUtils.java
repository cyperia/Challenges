package net.cyperia.commons.common.misc;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public final class PropertiesUtils {

  private PropertiesUtils() {
  }

  public static void setProperties(@NotNull Properties properties, @NotNull Map<String, Object> map) {
    for (Entry<Object, Object> entry : properties.entrySet()) {
      map.put((String) entry.getKey(), entry.getValue());
    }
  }

}
