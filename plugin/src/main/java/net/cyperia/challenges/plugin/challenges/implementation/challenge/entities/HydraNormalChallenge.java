package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.abstraction.HydraChallenge;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class HydraNormalChallenge extends HydraChallenge {

  @Override
  public int getNewMobsCount(@NotNull EntityType entityType) {
    return 2;
  }
}
