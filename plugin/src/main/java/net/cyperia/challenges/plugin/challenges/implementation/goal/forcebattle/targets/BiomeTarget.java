package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import com.google.gson.JsonParser;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.commons.common.misc.GsonUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BiomeTarget extends ForceTarget<Biome> {

  public BiomeTarget(Biome target) {
    super(target);
  }

  @Override
  public boolean check(Player player) {
    return player.getLocation().getBlock().getBiome() == target;
  }

  public static List<Biome> getPossibleBiomes() {
    return Arrays.stream(Biome.values())
      .filter(biome -> !biome.name().contains("VOID"))
      .filter(biome -> !biome.name().equals("CUSTOM"))
      .collect(Collectors.toList());
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.BIOME;
  }

  @Override
  public Object getTargetSaveObject() {
    return target.getKey().toString();
  }

  @NotNull
  public static BiomeTarget fromSaved(@NotNull Object saved) {
    return new BiomeTarget(RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).getOrThrow(savedKey(saved)));
  }

  // Older gamestates hold the bare enum name, or the key as a {namespace, key} object (as a map or as its JSON text)
  @NotNull
  private static NamespacedKey savedKey(@NotNull Object saved) {
    if (saved instanceof Map<?, ?> map) {
      return new NamespacedKey(savedKeyPart(map, "namespace"), savedKeyPart(map, "key"));
    }
    String text = saved.toString().trim();
    if (text.startsWith("{")) {
      return savedKey(GsonUtils.convertJsonObjectToMap(JsonParser.parseString(text).getAsJsonObject()));
    }
    NamespacedKey key = NamespacedKey.fromString(text.toLowerCase(Locale.ROOT));
    if (key == null) throw new IllegalArgumentException("Not a biome key: " + saved);
    return key;
  }

  @NotNull
  private static String savedKeyPart(@NotNull Map<?, ?> map, @NotNull String name) {
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (name.equalsIgnoreCase(String.valueOf(entry.getKey()))) {
        return String.valueOf(entry.getValue()).toLowerCase(Locale.ROOT);
      }
    }
    throw new IllegalArgumentException("Saved biome key has no " + name + ": " + map);
  }
}
