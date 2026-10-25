package net.cyperia.challenges.plugin.spigot.generator;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public abstract class ConvertingWorldGenerator extends ChunkGenerator {

  private final Material replacement;

  // New chunks whose leftovers are not placed yet. One that unloads before they are keeps its entry and is
  // cleaned on its next load, which is no longer a new one.
  private final Set<Long> pending = ConcurrentHashMap.newKeySet();

  protected ConvertingWorldGenerator(@NotNull Material replacement) {
    this.replacement = replacement;
  }

  protected abstract boolean replaces(int x, int y, int z, @NotNull Material type);

  // Structures still start in a chunk outside of this, so their positions keep matching the seed
  protected boolean generates(@NotNull WorldInfo worldInfo, int chunkX, int chunkZ) {
    return true;
  }

  @Override
  public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
    return generates(worldInfo, chunkX, chunkZ);
  }

  @Override
  public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
    return generates(worldInfo, chunkX, chunkZ);
  }

  @Override
  public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
    return generates(worldInfo, chunkX, chunkZ);
  }

  @Override
  public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
    return generates(worldInfo, chunkX, chunkZ);
  }

  @Override
  public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
    return generates(worldInfo, chunkX, chunkZ);
  }

  @Override
  public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
    return true;
  }

  // a populator and not ChunkData: populators run after vanilla's decoration step, which places trees, ores and structure blocks
  @NotNull
  @Override
  public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
    return List.of(new BlockPopulator() {
      @Override
      public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion region) {
        if (generates(worldInfo, chunkX, chunkZ)) convert(worldInfo, chunkX, chunkZ, region);
      }
    });
  }

  void convert(@NotNull WorldInfo worldInfo, int chunkX, int chunkZ, @NotNull LimitedRegion region) {
    int minX = chunkX << 4, minZ = chunkZ << 4;
    for (int x = minX; x < minX + 16; x++) {
      for (int z = minZ; z < minZ + 16; z++) {
        for (int y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight(); y++) {
          if (replaces(x, y, z, region.getType(x, y, z))) region.setType(x, y, z, replacement);
        }
      }
    }
  }

  // A neighbour decorating after this chunk's populator ran writes its trees, geodes and vines across the
  // border unconverted. A chunk only reaches LIGHT once every neighbour is past FEATURES, so on its first load
  // nothing can spill into it any more.
  public void cleanUp(@NotNull Chunk chunk, boolean newChunk) {
    long key = chunk.getChunkKey();
    if (newChunk) pending.add(key);
    else if (!pending.contains(key)) return;

    World world = chunk.getWorld();
    ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, false, false);
    int minY = world.getMinHeight();
    Challenges.getInstance().runAsync(() -> {
      int[] leftovers = leftovers(snapshot, minY, world.getMaxHeight());
      if (!Challenges.getInstance().isEnabled()) return;
      Regions.nextTick(Challenges.getInstance(), new Location(world, chunk.getX() << 4, 0, chunk.getZ() << 4), () -> {
        if (!chunk.isLoaded()) return;
        for (int position : leftovers) {
          chunk.getBlock(position & 15, minY + (position >> 8), position >> 4 & 15).setType(replacement, false);
        }
        pending.remove(key);
      });
    });
  }

  // Positions packed as (y - minY) << 8 | z << 4 | x
  int @NotNull [] leftovers(@NotNull ChunkSnapshot snapshot, int minY, int maxY) {
    int minX = snapshot.getX() << 4, minZ = snapshot.getZ() << 4;
    IntStream.Builder positions = IntStream.builder();
    for (int section = 0; section < (maxY - minY) >> 4; section++) {
      if (snapshot.isSectionEmpty(section)) continue;
      for (int index = section << 12; index < (section + 1) << 12; index++) {
        int x = index & 15, y = minY + (index >> 8), z = index >> 4 & 15;
        if (replaces(minX + x, y, minZ + z, snapshot.getBlockType(x, y, z))) positions.add(index);
      }
    }
    return positions.build().toArray();
  }

}
