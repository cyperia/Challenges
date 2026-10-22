package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.challenges.type.abstraction.LocationEffectChallenge;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.collection.SeededRandomWrapper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ChunkRandomEffectChallenge extends LocationEffectChallenge<Chunk> {

  @Override
  protected Chunk regionAt(@NotNull Location location) {
    return location.getChunk();
  }

  @NotNull
  @Override
  protected PotionEffect effectFor(@NotNull Chunk chunk) {
    IRandom random = new SeededRandomWrapper(worldSeed() * (
      (long) (chunk.getX() == 0 ? Integer.MAX_VALUE : chunk.getX()) * (chunk.getZ() == 0 ? Integer.MAX_VALUE : chunk.getZ())));

    PotionEffectType[] types = PotionEffectType.values();
    PotionEffectType type = types[random.nextInt(types.length)];

    return type.createEffect(PotionEffect.INFINITE_DURATION, random.nextInt(type.isInstant() ? 1 : 4));
  }

}
