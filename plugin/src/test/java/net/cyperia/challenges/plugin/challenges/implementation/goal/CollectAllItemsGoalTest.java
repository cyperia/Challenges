package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.utils.item.CollectableItem;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectAllItemsGoalTest {

  @Test
  void legacyListIsSortedIntoTheSeededOrder() {
    List<CollectableItem> seededOrder = List.of(CollectableItem.of(Material.DIRT), CollectableItem.of(Material.STONE),
      CollectableItem.of(Material.OAK_LOG), CollectableItem.of(Material.DIAMOND));

    assertEquals(List.of("minecraft:dirt", "minecraft:stone", "minecraft:diamond"),
      CollectAllItemsGoal.sortBySeededOrder(List.of("minecraft:diamond", "minecraft:dirt", "minecraft:stone"), seededOrder));
  }

  @Test
  void idsMissingFromTheSeededOrderComeFirst() {
    assertEquals(List.of("REMOVED_ITEM", "minecraft:dirt", "minecraft:stone"),
      CollectAllItemsGoal.sortBySeededOrder(List.of("minecraft:stone", "REMOVED_ITEM", "minecraft:dirt"),
        List.of(CollectableItem.of(Material.DIRT), CollectableItem.of(Material.STONE))));
  }

  @Test
  void legacyMaterialNamesBecomeIds() {
    assertEquals("minecraft:stone", CollectAllItemsGoal.normalizeId("STONE"));
    assertEquals("minecraft:stone", CollectAllItemsGoal.normalizeId("minecraft:stone"));
    assertEquals("REMOVED_ITEM", CollectAllItemsGoal.normalizeId("REMOVED_ITEM"));
  }

}
