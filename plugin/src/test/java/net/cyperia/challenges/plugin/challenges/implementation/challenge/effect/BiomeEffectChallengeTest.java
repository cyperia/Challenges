package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiomeEffectChallengeTest {

  private static final List<String> BIOMES = List.of(
    "minecraft:plains", "minecraft:desert", "minecraft:forest", "minecraft:taiga", "minecraft:swamp",
    "minecraft:jungle", "minecraft:savanna", "minecraft:badlands", "minecraft:ocean", "minecraft:river",
    "minecraft:snowy_plains", "minecraft:mushroom_fields", "minecraft:cherry_grove", "minecraft:dark_forest");
  private static final int TYPES = 33;

  @Test
  void aBiomeRollsTheSameEffectEveryTime() {
    assertEquals(BiomeEffectChallenge.roll("minecraft:plains", 42, TYPES), BiomeEffectChallenge.roll("minecraft:plains", 42, TYPES));
  }

  @Test
  void anotherWorldRollsAnotherSet() {
    assertNotEquals(rolls(42), rolls(1337));
  }

  @Test
  void biomesGetDifferentEffects() {
    Set<Integer> types = BIOMES.stream().map(biome -> BiomeEffectChallenge.roll(biome, 42, TYPES).type()).collect(Collectors.toSet());
    assertTrue(types.size() > BIOMES.size() / 2, "effects spread over the biomes: " + types);
  }

  @Test
  void rollsStayInsideTheEffectsAndLevels() {
    for (String biome : BIOMES) {
      BiomeEffectChallenge.Roll roll = BiomeEffectChallenge.roll(biome, -7_000_000_000L, TYPES);
      assertTrue(roll.type() >= 0 && roll.type() < TYPES);
      assertTrue(roll.level() >= 0 && roll.level() < BiomeEffectChallenge.LEVELS);
    }
  }

  private static List<BiomeEffectChallenge.Roll> rolls(long seed) {
    return BIOMES.stream().map(biome -> BiomeEffectChallenge.roll(biome, seed, TYPES)).toList();
  }

}
