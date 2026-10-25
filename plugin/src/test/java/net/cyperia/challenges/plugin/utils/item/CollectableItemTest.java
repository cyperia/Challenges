package net.cyperia.challenges.plugin.utils.item;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CollectableItemTest {

  @Test
  void plainItemRoundTrips() {
    CollectableItem stone = CollectableItem.of(Material.STONE);
    assertEquals("minecraft:stone", stone.id());
    assertEquals(stone, CollectableItem.parse(stone.id()));
  }

  @Test
  void legacyMaterialNameIsRead() {
    assertEquals(CollectableItem.of(Material.OAK_LOG), CollectableItem.parse("OAK_LOG"));
  }

  @Test
  void unknownMaterialIsNull() {
    assertNull(CollectableItem.parse("minecraft:not_an_item"));
  }

}
