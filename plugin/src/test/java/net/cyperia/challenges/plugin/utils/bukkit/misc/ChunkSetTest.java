package net.cyperia.challenges.plugin.utils.bukkit.misc;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChunkSetTest {

  @Test
  void removeMakesTheChunkNewAgainOnlyInItsWorld() {
    Chunk overworld = chunk("world", 3, -7);
    Chunk nether = chunk("world_nether", 3, -7);
    ChunkSet set = new ChunkSet();
    set.add(overworld);
    set.add(nether);

    set.remove("world", Chunk.getChunkKey(3, -7));

    assertFalse(set.contains(overworld));
    assertTrue(set.contains(nether));
    assertTrue(set.add(overworld));
  }

  private static Chunk chunk(String worldName, int x, int z) {
    World world = mock(World.class);
    when(world.getName()).thenReturn(worldName);
    Chunk chunk = mock(Chunk.class);
    when(chunk.getWorld()).thenReturn(world);
    when(chunk.getChunkKey()).thenReturn(Chunk.getChunkKey(x, z));
    return chunk;
  }

}
