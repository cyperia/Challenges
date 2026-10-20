package net.cyperia.challenges.plugin.spigot.generator;

import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class VoidMapGenerator extends ChunkGenerator {

  public static final int SPAWN_BLOCK_Y = 59;
  private static final NamespacedKey CLEANED = new NamespacedKey("challenges", "void_structure_cleaned");
  // the blocks of the icebergs frozen oceans decorate with, the one decoration that builds into open air
  private static final Set<Material> ICEBERG = Set.of(Material.PACKED_ICE, Material.BLUE_ICE, Material.SNOW_BLOCK);

  private final boolean structures;
  private volatile StructureFootprints footprints;
  private volatile VanillaTerrain terrain;
  private volatile boolean terrainUnavailable;

  public VoidMapGenerator() {
    this(false);
  }

  // With structures, vanilla ones still generate, floating in the void
  public VoidMapGenerator(boolean structures) {
    this.structures = structures;
  }

  @Override
  public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    if (chunkX == 0 && chunkZ == 0) {
      chunkData.setBlock(0, SPAWN_BLOCK_Y, 0, Material.BEDROCK);
    }
    if (worldInfo.getEnvironment() == Environment.NORMAL) {
      fillCarvedStructures(worldInfo, chunkX, chunkZ, chunkData);
      placeStructureSupport(worldInfo, chunkX, chunkZ, chunkData);
    }
  }

  // a stronghold only replaces blocks that are already there, so it gets bedrock to carve its corridors out of, removed again by cleanUp;
  // bedrock because decorations like geodes may not replace it, where they would grow inside barriers and float around the stronghold
  private void fillCarvedStructures(@NotNull WorldInfo worldInfo, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    StructureFootprints footprints = getFootprints(worldInfo.getUID());
    if (footprints == null) return;
    for (StructureFootprints.Footprint footprint : footprints.getCarved(chunkX, chunkZ)) {
      chunkData.setRegion(
        Math.max(footprint.minX() - (chunkX << 4), 0), Math.max(footprint.minY(), chunkData.getMinHeight()), Math.max(footprint.minZ() - (chunkZ << 4), 0),
        Math.min(footprint.maxX() - (chunkX << 4) + 1, 16), Math.min(footprint.maxY() + 1, chunkData.getMaxHeight()), Math.min(footprint.maxZ() - (chunkZ << 4) + 1, 16),
        Material.BEDROCK);
    }
  }

  // Overworld structures fit themselves to the ground while they are built, so barriers stand in for the vanilla ground under them until cleanUp
  private void placeStructureSupport(@NotNull WorldInfo worldInfo, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    StructureFootprints footprints = getFootprints(worldInfo.getUID());
    if (footprints == null || !footprints.covers(chunkX, chunkZ)) return;
    VanillaTerrain terrain = getTerrain(worldInfo.getUID());
    if (terrain == null) return;

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        int blockX = (chunkX << 4) + x, blockZ = (chunkZ << 4) + z;
        if (!footprints.contains(blockX, blockZ)) continue;
        int groundY = terrain.getHeight(blockX, blockZ) - 1;
        if (groundY >= chunkData.getMinHeight() && groundY < chunkData.getMaxHeight()) {
          chunkData.setBlock(x, groundY, z, Material.BARRIER);
        }
      }
    }
  }

  @Override
  public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
    return new Location(world, 0.5, SPAWN_BLOCK_Y + 1, 0.5);
  }

  @Override
  public boolean shouldGenerateStructures() {
    return structures;
  }

  // the server places a structure's blocks together with the decorations, so exactly the chunks a structure covers are decorated
  @Override
  public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
    StructureFootprints footprints = getFootprints(worldInfo.getUID());
    return footprints != null && footprints.covers(chunkX, chunkZ);
  }

  public void addStructure(@NotNull World world, @NotNull Structure structure, @NotNull BoundingBox box) {
    StructureFootprints footprints = getFootprints(world.getUID());
    if (footprints != null) footprints.add(box, structure.equals(Structure.STRONGHOLD));
  }

  /**
   * Removes the barriers and every decoration outside the structures of a decorated chunk, once it is loaded.
   */
  public void cleanUp(@NotNull Chunk chunk) {
    StructureFootprints footprints = getFootprints(chunk.getWorld().getUID());
    if (footprints == null || !footprints.covers(chunk.getX(), chunk.getZ())) return;
    // a chunk only loads once its neighbours are decorated, which is the last time anything is placed into it
    if (chunk.getPersistentDataContainer().has(CLEANED)) return;
    chunk.getPersistentDataContainer().set(CLEANED, PersistentDataType.BOOLEAN, true);

    World world = chunk.getWorld();
    ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, true, false);
    List<StructureFootprints.Footprint> carved = footprints.getCarved(chunk.getX(), chunk.getZ());
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        boolean underStructure = footprints.contains((chunk.getX() << 4) + x, (chunk.getZ() << 4) + z);
        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
          Material type = snapshot.getBlockType(x, y, z);
          if (type.isAir() || isSpawnBlock(chunk, x, y, z)) continue;

          if (type == Material.BEDROCK && isCarved(carved, (chunk.getX() << 4) + x, y, (chunk.getZ() << 4) + z)) {
            chunk.getBlock(x, y, z).setType(Material.AIR, false);
          } else if (type == Material.BARRIER) {
            chunk.getBlock(x, y, z).setType(Material.AIR, false);
            // snow settles on the topmost block, which the barrier was
            if (y + 1 < world.getMaxHeight() && snapshot.getBlockType(x, y + 1, z) == Material.SNOW) {
              chunk.getBlock(x, y + 1, z).setType(Material.AIR, false);
            }
          } else if (!underStructure || ICEBERG.contains(type) && isFrozenOcean(snapshot.getBiome(x, y, z))) {
            chunk.getBlock(x, y, z).setType(Material.AIR, false);
          }
        }
      }
    }
  }

  private static boolean isCarved(@NotNull List<StructureFootprints.Footprint> carved, int x, int y, int z) {
    for (StructureFootprints.Footprint footprint : carved) {
      if (x >= footprint.minX() && x <= footprint.maxX() && y >= footprint.minY() && y <= footprint.maxY()
        && z >= footprint.minZ() && z <= footprint.maxZ()) return true;
    }
    return false;
  }

  private static boolean isSpawnBlock(@NotNull Chunk chunk, int x, int y, int z) {
    return chunk.getX() == 0 && chunk.getZ() == 0 && x == 0 && z == 0 && y == SPAWN_BLOCK_Y;
  }

  private static boolean isFrozenOcean(@NotNull Biome biome) {
    return biome.equals(Biome.FROZEN_OCEAN) || biome.equals(Biome.DEEP_FROZEN_OCEAN);
  }

  @Nullable
  private StructureFootprints getFootprints(@NotNull UUID worldId) {
    if (!structures) return null;
    if (footprints != null) return footprints;
    synchronized (this) {
      if (footprints == null) {
        World world = Bukkit.getWorld(worldId);
        if (world == null) return null;
        footprints = new StructureFootprints(world.getWorldFolder().toPath().resolve("challenges-structures.bin"));
      }
      return footprints;
    }
  }

  @Nullable
  private VanillaTerrain getTerrain(@NotNull UUID worldId) {
    if (terrain != null || terrainUnavailable) return terrain;
    synchronized (this) {
      if (terrain == null && !terrainUnavailable) {
        World world = Bukkit.getWorld(worldId);
        if (world == null) return null;
        try {
          terrain = new VanillaTerrain(world);
        } catch (ReflectiveOperationException | RuntimeException exception) {
          terrainUnavailable = true;
          Logger.warn("Unable to read the vanilla terrain height, structures that fit themselves to the ground generate at the bottom", exception);
        }
      }
      return terrain;
    }
  }

}
