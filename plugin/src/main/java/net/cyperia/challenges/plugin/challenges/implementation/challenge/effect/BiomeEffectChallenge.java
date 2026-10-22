package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.challenges.type.abstraction.LocationEffectChallenge;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.collection.SeededRandomWrapper;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BiomeEffectChallenge extends LocationEffectChallenge<Biome> {

  // Biomes are large and slow to leave, so the effect stays at level I or II
  static final int LEVELS = 2;

  // Read on first use: the effect registry only exists once a server runs
  private PotionEffectType[] types;

  @Override
  protected Biome regionAt(@NotNull Location location) {
    return location.getBlock().getBiome();
  }

  @NotNull
  @Override
  protected PotionEffect effectFor(@NotNull Biome biome) {
    if (types == null) {
      types = Arrays.stream(PotionEffectType.values()).filter(BiomeEffectChallenge::fitsABiome).toArray(PotionEffectType[]::new);
    }
    Roll roll = roll(biome.getKey().toString(), worldSeed(), types.length);
    return types[roll.type()].createEffect(PotionEffect.INFINITE_DURATION, roll.level());
  }

  // Instant effects would fire again every second for as long as a player stays in the biome, and an omen that never
  // runs out either never resolves or starts a raid in the first village
  private static boolean fitsABiome(@NotNull PotionEffectType type) {
    return !type.isInstant() && !type.getKey().getKey().endsWith("_omen");
  }

  // Seeded by world and biome alone, so a restart keeps every biome's effect
  static Roll roll(@NotNull String biomeKey, long worldSeed, int typeCount) {
    IRandom random = new SeededRandomWrapper(worldSeed * 31 + biomeKey.hashCode());
    return new Roll(random.nextInt(typeCount), random.nextInt(LEVELS));
  }

  record Roll(int type, int level) {}

}
