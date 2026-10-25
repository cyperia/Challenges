package net.cyperia.challenges.plugin.utils.entity;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobVariantsTest {

  @Test
  void everyVariantHasItsOwnId() {
    Set<String> ids = new HashSet<>();
    MobVariants.load().values().forEach(mobs -> mobs.forEach(mob -> assertTrue(ids.add(mob.id()), mob.id())));
  }

  @Test
  void everyVariantBelongsToItsType() {
    MobVariants.load().forEach((type, mobs) -> mobs.forEach(mob -> assertEquals(type, mob.type())));
  }

  @Test
  void villagersComeInEveryProfessionAndBiome() {
    Map<EntityType, List<KillableMob>> variants = MobVariants.load();
    // 15 professions in 7 biomes, and a baby per biome
    assertEquals(15 * 7 + 7, variants.get(EntityType.VILLAGER).size());
  }

  @Test
  void aTypeOnItsOwnKeepsTheIdItHadBeforeVariants() {
    assertEquals("minecraft:zombie", KillableMob.of(EntityType.ZOMBIE).id());
    assertEquals("minecraft:zombie", KillableMob.normalizeId("ZOMBIE"));
    assertEquals("minecraft:zombie[baby]", KillableMob.normalizeId("minecraft:zombie[baby]"));
  }

  @Test
  void thePoolListsEachTypeBeforeItsVariants() {
    List<KillableMob> pool = KillableMob.pool(List.of(EntityType.CREEPER), true, true);
    assertEquals(List.of("minecraft:creeper", "minecraft:creeper[charged]"), pool.stream().map(KillableMob::id).toList());
    assertEquals(1, KillableMob.pool(List.of(EntityType.CREEPER), false, false).size());
  }

  @Test
  void aTypeTheVariantsCoverIsNotListedOnItsOwn() {
    assertEquals(List.of("minecraft:salmon[size=small]", "minecraft:salmon[size=medium]", "minecraft:salmon[size=large]"),
      KillableMob.pool(List.of(EntityType.SALMON), true, true).stream().map(KillableMob::id).toList());
    assertEquals(List.of("minecraft:salmon"), KillableMob.pool(List.of(EntityType.SALMON), false, false).stream().map(KillableMob::id).toList());
  }

  @Test
  void noVariantMixesAttributesWithEquipment() {
    MobVariants.load().values().forEach(mobs -> mobs.forEach(mob ->
      assertTrue(mob.traits().stream().allMatch(MobTrait::isEquipment) || mob.traits().stream().noneMatch(MobTrait::isEquipment), mob.id())));
  }

  @Test
  void equipmentAloneKeepsOnlyWhatTheMobHoldsAndWears() {
    List<String> ids = KillableMob.pool(List.of(EntityType.ZOMBIE), false, true).stream().map(KillableMob::id).toList();
    assertTrue(ids.contains("minecraft:zombie"));
    assertTrue(ids.contains("minecraft:zombie[holding=iron_sword]"));
    assertTrue(ids.contains("minecraft:zombie[armor=diamond]"));
    assertTrue(ids.contains("minecraft:zombie[riding=chicken]"));
    assertTrue(ids.stream().noneMatch(id -> id.contains("handed") || id.contains("baby")), ids.toString());
    assertEquals(ids.size(), Set.copyOf(ids).size());
  }

  @Test
  void attributesAloneLeaveTheEquipmentOut() {
    List<String> ids = KillableMob.pool(List.of(EntityType.ZOMBIE), true, false).stream().map(KillableMob::id).toList();
    assertTrue(ids.contains("minecraft:zombie[baby]"));
    assertTrue(ids.contains("minecraft:zombie[left-handed]"));
    assertTrue(ids.stream().noneMatch(id -> id.contains("holding") || id.contains("armor")), ids.toString());
  }

  @Test
  void equipmentAloneListsACoveredTypeOnItsOwn() {
    List<String> ids = KillableMob.pool(List.of(EntityType.FOX), false, true).stream().map(KillableMob::id).toList();
    assertEquals("minecraft:fox", ids.getFirst());
    assertTrue(ids.contains("minecraft:fox[holding=emerald]"), ids.toString());
  }

  @Test
  void everyCoveredTypeHasVariants() {
    Map<EntityType, List<KillableMob>> variants = MobVariants.load();
    for (EntityType type : EntityType.values()) {
      if (MobVariants.coversEveryMob(type)) assertTrue(variants.containsKey(type), type.name());
    }
  }

}
