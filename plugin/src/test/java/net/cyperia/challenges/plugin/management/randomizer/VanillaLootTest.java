package net.cyperia.challenges.plugin.management.randomizer;

import com.google.gson.JsonParser;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// the tables are copies of the vanilla 26.3 loot tables
class VanillaLootTest {

  private static Set<Material> collect(String table) {
    Set<Material> items = new LinkedHashSet<>();
    var in = Objects.requireNonNull(VanillaLootTest.class.getResourceAsStream("/loot/" + table + ".json"));
    VanillaLoot.collect(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)), items);
    return items;
  }

  @Test
  void skipsTheSilkTouchDrop() {
    assertEquals(Set.of(Material.COBBLESTONE), collect("stone"));
  }

  @Test
  void keepsDropsBehindAnInvertedSilkTouchCheck() {
    Set<Material> leaves = collect("oak_leaves");
    assertTrue(leaves.containsAll(Set.of(Material.OAK_SAPLING, Material.STICK, Material.APPLE)), leaves.toString());
  }

  @Test
  void readsAnEntityTable() {
    assertEquals(Set.of(Material.BLAZE_ROD), collect("blaze"));
  }

}
