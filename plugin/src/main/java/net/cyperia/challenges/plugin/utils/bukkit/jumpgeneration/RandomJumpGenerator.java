package net.cyperia.challenges.plugin.utils.bukkit.jumpgeneration;

import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.block.Block;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;


public class RandomJumpGenerator implements IJumpGenerator {

  @NotNull
  @Override
  @CheckReturnValue
  public Block next(@NotNull IRandom random, @NotNull Block startingPoint, boolean includeFourBlockJumps, boolean includeUpGoing) {

    int layer = random.nextInt(includeUpGoing ? 3 : 2) - 1;
    int range = layer == 0 ? 4 : 3;

    int mainDirection = random.nextInt(range * 2) - range;
    int mainDirectionValue = Math.abs(mainDirection);
    if (mainDirectionValue < 2) {
      mainDirection = random.choose(-range, range);
      mainDirectionValue = Math.abs(mainDirection);
    }

    int secondDirection = determineSecondDirection(random, mainDirectionValue, range);
    return translate(startingPoint, random, layer, mainDirection, secondDirection);

  }

  protected int determineSecondDirection(@NotNull IRandom random, int mainDirection, int range) {
    if (mainDirection == range || mainDirection == range - 1) {
      return random.choose(-1, 0, 1);
    } else if (mainDirection == range - 2) {
      return random.choose(-2, -1, 0, 1, 2);
    } else if (mainDirection == range - 3) {
      return random.choose(-3, -2, -1, 0, 1, 2, 3);
    } else if (mainDirection == range - 4 || mainDirection == range - 5) {
      return random.choose(-4, -3, -2, -1, 0, 1, 2, 3, 4);
    }
    throw new IllegalArgumentException("Could not determine second direction for main direction " + mainDirection + ", range " + range);
  }

  @NotNull
  @CheckReturnValue
  protected Block translate(@NotNull Block startingPoint, @NotNull IRandom random, int layer, int mainDirection, int secondDirection) {

    boolean intoX = random.nextBoolean();

    int x = intoX ? mainDirection : secondDirection;
    int z = intoX ? secondDirection : mainDirection;

    return startingPoint.getRelative(x, layer, z);

  }

}
