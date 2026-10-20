package net.cyperia.challenges.plugin.spigot.generator;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

// the height the vanilla terrain of a world would have, which the API does not expose once a plugin generates the world;
// read through the server's own generator by reflection, with the mojang names paper runs on
final class VanillaTerrain {

  private final Object level;
  private final Object generator;
  private final Object randomState;
  private final Object oceanFloor;
  private final Method getBaseHeight;

  VanillaTerrain(@NotNull World world) throws ReflectiveOperationException {
    level = world.getClass().getMethod("getHandle").invoke(world);
    Object chunkSource = level.getClass().getMethod("getChunkSource").invoke(level);
    Object pluginGenerator = chunkSource.getClass().getMethod("getGenerator").invoke(chunkSource);
    generator = pluginGenerator.getClass().getMethod("getDelegate").invoke(pluginGenerator);
    randomState = chunkSource.getClass().getMethod("randomState").invoke(chunkSource);

    Class<?> heightmapTypes = Class.forName("net.minecraft.world.level.levelgen.Heightmap$Types", false, generator.getClass().getClassLoader());
    oceanFloor = heightmapTypes.getField("OCEAN_FLOOR_WG").get(null);
    getBaseHeight = Arrays.stream(generator.getClass().getMethods())
      .filter(method -> method.getName().equals("getBaseHeight") && method.getParameterCount() == 5)
      .findFirst()
      .orElseThrow(() -> new NoSuchMethodException("getBaseHeight"));
  }

  // the first free block above the ground, ignoring water
  int getHeight(int x, int z) {
    try {
      return (int) getBaseHeight.invoke(generator, x, z, oceanFloor, level, randomState);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(exception);
    }
  }

}
