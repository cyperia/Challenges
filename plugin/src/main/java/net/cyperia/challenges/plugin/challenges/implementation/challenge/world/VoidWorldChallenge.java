package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.abstraction.WorldGeneratorChallenge;
import net.cyperia.challenges.plugin.spigot.generator.VoidMapGenerator;
import org.bukkit.World.Environment;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoidWorldChallenge extends WorldGeneratorChallenge {

  @Nullable
  @Override
  protected ChunkGenerator createGenerator(@NotNull Environment environment) {
    return environment == Environment.THE_END ? null : new VoidMapGenerator(true);
  }

  // A stronghold is the only way into the end, and fortresses hold the blazes its eyes of ender need
  @Override
  protected boolean generatesStructure(@NotNull Environment environment, @NotNull Structure structure) {
    return structure.equals(Structure.STRONGHOLD) || structure.equals(Structure.FORTRESS);
  }

}
