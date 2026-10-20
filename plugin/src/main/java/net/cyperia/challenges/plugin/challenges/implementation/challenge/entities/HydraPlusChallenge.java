package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.abstraction.HydraChallenge;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class HydraPlusChallenge extends HydraChallenge {

  private static final int limit = 512;

  @Override
  public int getNewMobsCount(@NotNull EntityType entityType) {
    int currentCount = getGameStateData().getInt(entityType.name());
    currentCount = currentCount == 0 ? 2 : Math.min(currentCount * 2, limit);
    getGameStateData().set(entityType.name(), currentCount);
    return currentCount;
  }

}
