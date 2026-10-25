package net.cyperia.challenges.plugin.utils.misc;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantIdsTest {

  @Test
  void aVariantCoversItsBaseAndEveryVariantOfFewerParts() {
    String found = "minecraft:zombie[left-handed,holding=iron_sword]";
    assertTrue(VariantIds.covers(found, "minecraft:zombie"));
    assertTrue(VariantIds.covers(found, "minecraft:zombie[holding=iron_sword]"));
    assertTrue(VariantIds.covers(found, "minecraft:zombie[left-handed]"));
  }

  @Test
  void aVariantCoversNothingElse() {
    String found = "minecraft:zombie[left-handed,holding=iron_sword]";
    assertFalse(VariantIds.covers(found, found));
    assertFalse(VariantIds.covers(found, "minecraft:zombie[baby]"));
    assertFalse(VariantIds.covers(found, "minecraft:husk"));
    assertFalse(VariantIds.covers("minecraft:zombie", "minecraft:zombie[baby]"));
    assertFalse(VariantIds.covers("mob/minecraft:zombie[baby]", "minecraft:zombie"));
  }

  @Test
  void forEachCoveredPairsFindsWithThePoolIdsTheyCount() {
    List<String> pairs = new ArrayList<>();
    VariantIds.forEachCovered(List.of("item/minecraft:enchanted_book[minecraft:sharpness=5]"),
      Set.of("item/minecraft:enchanted_book", "item/minecraft:stick"), (id, target) -> pairs.add(target));
    assertEquals(List.of("item/minecraft:enchanted_book"), pairs);
  }

}
