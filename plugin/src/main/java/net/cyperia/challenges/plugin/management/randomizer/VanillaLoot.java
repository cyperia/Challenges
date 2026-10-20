package net.cyperia.challenges.plugin.management.randomizer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// reads the loot tables shipped in the server jar, so a datapack that changes them is not seen
public final class VanillaLoot {

  private static final Map<String, Set<Material>> TABLES = new HashMap<>();

  private VanillaLoot() {
  }

  @NotNull
  public static Set<Material> of(@NotNull Material block) {
    return of("blocks/" + block.getKey().getKey());
  }

  @NotNull
  public static Set<Material> of(@NotNull EntityType entity) {
    return of("entities/" + entity.getKey().getKey());
  }

  @NotNull
  private static Set<Material> of(@NotNull String table) {
    Set<Material> items = TABLES.get(table);
    if (items != null) return items;
    // marks the table before reading it, so a table that references itself ends the recursion
    TABLES.put(table, Set.of());
    items = Collections.unmodifiableSet(read(table));
    TABLES.put(table, items);
    return items;
  }

  @NotNull
  private static Set<Material> read(@NotNull String table) {
    Set<Material> items = new LinkedHashSet<>();
    String path = "data/minecraft/loot_table/" + table + ".json";
    try (InputStream in = Bukkit.getServer().getClass().getClassLoader().getResourceAsStream(path)) {
      if (in == null) return items;
      collect(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)), items);
    } catch (IOException | RuntimeException exception) {
      return items;
    }
    return items;
  }

  // every item an entry can drop, ignoring chances and conditions except silk touch, which a player rarely has
  static void collect(@NotNull JsonElement element, @NotNull Set<Material> items) {
    if (element.isJsonArray()) {
      element.getAsJsonArray().forEach(child -> collect(child, items));
      return;
    }
    if (!element.isJsonObject()) return;

    JsonObject object = element.getAsJsonObject();
    if (requiresSilkTouch(object.get("condition")) || requiresSilkTouch(object.get("conditions"))) return;

    String type = string(object.get("type"));
    if ("minecraft:item".equals(type)) {
      Material item = Material.matchMaterial(String.valueOf(string(object.get("name"))));
      if (item != null) items.add(item);
    } else if ("minecraft:loot_table".equals(type)) {
      String table = string(object.get("value"));
      if (table != null) items.addAll(of(table.substring(table.indexOf(':') + 1)));
    }

    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      if (entry.getKey().equals("condition") || entry.getKey().equals("conditions")) continue;
      collect(entry.getValue(), items);
    }
  }

  static boolean requiresSilkTouch(@Nullable JsonElement condition) {
    if (condition == null) return false;
    // a condition can also name a predicate file, like minecraft:tool/can_silk_touch
    if (condition.isJsonPrimitive()) return condition.getAsString().contains("silk_touch");
    if (condition.isJsonArray()) {
      for (JsonElement term : condition.getAsJsonArray()) {
        if (requiresSilkTouch(term)) return true;
      }
      return false;
    }
    if (!condition.isJsonObject()) return false;

    JsonObject object = condition.getAsJsonObject();
    String type = String.valueOf(string(object.get("type")));
    return switch (type) {
      case "minecraft:match_tool" -> object.toString().contains("minecraft:silk_touch");
      case "minecraft:all_of" -> requiresSilkTouch(object.get("terms"));
      case "minecraft:any_of" -> {
        if (object.get("terms") == null || !object.get("terms").isJsonArray()) yield false;
        boolean all = true;
        for (JsonElement term : object.getAsJsonArray("terms")) all &= requiresSilkTouch(term);
        yield all;
      }
      default -> false;
    };
  }

  @Nullable
  private static String string(@Nullable JsonElement element) {
    return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
  }

}
