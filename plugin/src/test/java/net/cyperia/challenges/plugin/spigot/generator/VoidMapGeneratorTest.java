package net.cyperia.challenges.plugin.spigot.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class VoidMapGeneratorTest {

  private final VoidMapGenerator generator = new VoidMapGenerator(true);

  @Test
  void spawnStandsOnTheOnlyBlock() {
    ChunkData origin = mock(ChunkData.class);
    generator.generateSurface(mock(WorldInfo.class), new Random(), 0, 0, origin);
    verify(origin).setBlock(0, 59, 0, Material.BEDROCK);

    World world = mock(World.class);
    assertEquals(new Location(world, 0.5, 60, 0.5), generator.getFixedSpawnLocation(world, new Random()));
  }

  @Test
  void otherChunksStayEmpty() {
    ChunkData chunk = mock(ChunkData.class);
    generator.generateSurface(mock(WorldInfo.class), new Random(), 1, 0, chunk);
    verify(chunk, never()).setBlock(anyInt(), anyInt(), anyInt(), any(Material.class));
  }

  @Test
  void structuresOnlyWhenAskedFor() {
    assertTrue(generator.shouldGenerateStructures());
    assertFalse(new VoidMapGenerator().shouldGenerateStructures());
  }

}
