package net.cyperia.challenges.plugin.challenges.implementation.goal;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectAllItemsGoalTest {

  @Test
  void legacyListIsSortedIntoTheSeededOrder() {
    List<Material> seededOrder = List.of(Material.DIRT, Material.STONE, Material.OAK_LOG, Material.DIAMOND);

    assertEquals(List.of("DIRT", "STONE", "DIAMOND"),
      CollectAllItemsGoal.sortBySeededOrder(List.of("DIAMOND", "DIRT", "STONE"), seededOrder));
  }

  @Test
  void namesMissingFromTheSeededOrderComeFirst() {
    assertEquals(List.of("REMOVED_ITEM", "DIRT", "STONE"),
      CollectAllItemsGoal.sortBySeededOrder(List.of("STONE", "REMOVED_ITEM", "DIRT"), List.of(Material.DIRT, Material.STONE)));
  }

}
