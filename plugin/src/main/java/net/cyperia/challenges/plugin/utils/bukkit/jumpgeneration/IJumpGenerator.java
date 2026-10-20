package net.cyperia.challenges.plugin.utils.bukkit.jumpgeneration;

import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.block.Block;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IJumpGenerator {

  @NotNull
  @CheckReturnValue
  Block next(@NotNull IRandom random, @NotNull Block startingPoint, boolean includeFourBlockJumps, boolean includeUpGoing);

}
