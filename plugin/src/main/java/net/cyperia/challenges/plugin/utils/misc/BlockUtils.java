package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BlockUtils {

  private static final BlockFace[] faces = {
    BlockFace.UP, BlockFace.DOWN,
    BlockFace.NORTH, BlockFace.EAST,
    BlockFace.SOUTH, BlockFace.WEST
  };

  // How far below the feet a block still counts as stood on, for positions the client rounds slightly high.
  private static final double GROUND_TOLERANCE = 0.05;

  private BlockUtils() {
  }

  // World#getSpawnLocation is the spawn block's corner, which puts a player half inside the neighbouring blocks
  @NotNull
  public static Location getCenteredSpawn(@NotNull World world) {
    return world.getSpawnLocation().toBlockLocation().add(0.5, 0, 0.5);
  }

  // Underground a spot is rock as often as not, and an entity put inside blocks suffocates.
  @NotNull
  public static Location freeSpot(@NotNull Location location) {
    Block block = location.getBlock();
    if (!block.isPassable() || !block.getRelative(BlockFace.UP).isPassable()) {
      location.setY(location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1.0);
    }
    return location;
  }

  public static boolean isSameBlockLocation(@Nullable Location loc1, @Nullable Location loc2) {
    if (loc1 == null || loc2 == null) return true;
    if (loc1.getWorld() != loc2.getWorld()) return false;
    return loc1.getBlockX() == loc2.getBlockX()
      && loc1.getBlockY() == loc2.getBlockY()
      && loc1.getBlockZ() == loc2.getBlockZ();
  }

  public static boolean isSameBlockLocationIgnoreHeight(@Nullable Location loc1, @Nullable Location loc2) {
    if (loc1 == null || loc2 == null) return false;
    if (loc1.getWorld() != loc2.getWorld()) return false;
    return loc1.getBlockX() == loc2.getBlockX()
      && loc1.getBlockZ() == loc2.getBlockZ();
  }

  public static boolean isSameLocation(@NotNull Location loc1, @NotNull Location loc2) {
    if (loc1.getWorld() != loc2.getWorld()) return false;
    return loc1.distance(loc2) < 0.1;
  }

  public static boolean isSameLocationIgnoreHeight(@NotNull Location loc1, @NotNull Location loc2) {
    if (loc1.getWorld() != loc2.getWorld()) return false;
    return loc1.getX() == loc2.getX()
      && loc1.getZ() == loc2.getZ();
  }

  public static boolean isSameChunk(@NotNull Chunk chunk1, @NotNull Chunk chunk2) {
    if (chunk1.getWorld() != chunk2.getWorld()) return false;
    return chunk1.getX() == chunk2.getX() && chunk1.getZ() == chunk2.getZ();
  }

  public static boolean isSameBlockIgnoreHeight(@Nullable Location loc1, @Nullable Location loc2) {
    if (loc1 == null || loc2 == null) return false;
    return loc1.getBlockX() == loc2.getBlockX()
      && loc1.getBlockZ() == loc2.getBlockZ();
  }

  /**
   * @param block middle block
   * @return the block above, under, in the front, behind, to the left and to the right of the middle block
   */
  @NotNull
  public static List<Block> getBlocksAroundBlock(@NotNull Block block) {
    List<Block> list = new ArrayList<>();
    for (BlockFace face : faces) {
      list.add(block.getRelative(face));
    }
    return list;
  }

  public static Material getTerracotta(int subId) {
    switch (subId) {
      case 2:
        return Material.ORANGE_TERRACOTTA;
      case 3:
        return Material.MAGENTA_TERRACOTTA;
      case 4:
        return Material.LIGHT_BLUE_TERRACOTTA;
      case 5:
        return Material.YELLOW_TERRACOTTA;
      case 6:
        return Material.LIME_TERRACOTTA;
      case 7:
        return Material.PINK_TERRACOTTA;
      case 8:
        return Material.GRAY_TERRACOTTA;
      case 9:
        return Material.LIGHT_GRAY_TERRACOTTA;
      case 10:
        return Material.CYAN_TERRACOTTA;
      case 11:
        return Material.PURPLE_TERRACOTTA;
      case 12:
        return Material.BLUE_TERRACOTTA;
      case 13:
        return Material.BROWN_TERRACOTTA;
      case 14:
        return Material.GREEN_TERRACOTTA;
      case 15:
        return Material.RED_TERRACOTTA;
      case 16:
        return Material.BLACK_TERRACOTTA;
      default:
        return Material.WHITE_TERRACOTTA;
    }
  }

  /**
   * Sets the material of the block and breaks snow or other not solid blocks on top of it
   *
   * @param block the block of block to replace
   * @param type  the type to set as the block type
   */
  public static void setBlockNatural(@Nullable Block block, @NotNull Material type, boolean blockUpdate) {
    setBlockNatural(block, type, blockUpdate, true);
  }

  /**
   * Sets the material of the block and breaks snow or other not solid blocks on top of it
   *
   * @param block     the block of block to replace
   * @param type      the type to set as the block type
   * @param playSound if a breaking sound for the block on top should be played
   */
  public static void setBlockNatural(@Nullable Block block, @NotNull Material type, boolean blockUpdate, boolean playSound) {
    if (block == null || !block.getType().isSolid()) return;

    Block upperBlock = block.getLocation().add(0, 1, 0).getBlock();

    if (!upperBlock.getType().isSolid()) {
      upperBlock.breakNaturally();
      // TODO: PLAY THE RIGHT BREAKING SOUND FOR THE BLOCK

    }

    block.setType(type, blockUpdate);
  }

  /**
   * @param location the location to get the block below
   * @return the block below the location
   */
  @Nullable
  public static Block getBlockBelow(@NotNull Location location) {
    return getBlockBelow(location, 0.1);
  }

  /**
   * @param location the location to get the block below
   * @return the block below the location
   */
  @Nullable
  public static Block getBlockBelow(@NotNull Location location, boolean ignoreNonSolid) {
    return getBlockBelow(location, 0.1, ignoreNonSolid);
  }

  /**
   * @param location the location to get the block below
   * @return the block below the location
   */
  @Nullable
  public static Block getBlockBelow(@NotNull Location location, double offset) {
    return getBlockBelow(location, offset, true);
  }

  /**
   * @param location the location to get the block below
   * @return the block below the location
   */
  @Nullable
  public static Block getBlockBelow(@NotNull Location location, double offset, boolean ignoreNonSolid) {

    Block block;
    if (offset == -1) {
      block = getBlockBelow(location, 0.1, ignoreNonSolid);
      if (block == null) {
        block = getBlockBelow(location, 1, ignoreNonSolid);
        if (block == null) {
          block = getBlockBelow(location, 1.5, ignoreNonSolid);
        }
      }

    } else {
      block = location.clone().subtract(0, offset, 0).getBlock();
    }

    if (block == null) return null;
    if (ignoreNonSolid && !block.getType().isSolid()) {
      return null;
    }
    return block;
  }

  public static boolean isEndItem(Material material) {
    String name = material.name();
    return material == Material.ELYTRA ||
      name.contains("PURPUR") ||
      name.contains("SHULKER") ||
      name.contains("END");
  }

  public static boolean isTooHardToGet(Material material) {
    String name = material.name();
    return name.contains("EXPOSED") ||
      name.contains("WEATHERED") ||
      name.contains("OXIDIZED") ||
      name.contains("BUD");
  }

  // The block carrying the player, found under any part of their hitbox rather than only under their
  // position, so standing on an edge counts. Across several blocks, the one nearest the centre wins, as in
  // vanilla. Below-feet cells go deeper than one block for fences and walls, which stick out of their cell.
  @Nullable
  public static Block getGround(@NotNull World world, @NotNull BoundingBox hitbox) {
    double feet = hitbox.getMinY();
    BoundingBox probe = new BoundingBox(hitbox.getMinX(), feet - GROUND_TOLERANCE, hitbox.getMinZ(), hitbox.getMaxX(), feet, hitbox.getMaxZ());

    Block ground = null;
    double nearest = Double.MAX_VALUE;
    for (int x = floor(hitbox.getMinX()); x <= floor(hitbox.getMaxX()); x++) {
      for (int z = floor(hitbox.getMinZ()); z <= floor(hitbox.getMaxZ()); z++) {
        // Top down, so a carpet wins over the block it lies on.
        for (int y = floor(feet); y >= floor(feet - 0.6); y--) {
          Block block = world.getBlockAt(x, y, z);
          if (!block.getCollisionShape().overlaps(probe.clone().shift(-x, -y, -z))) continue;
          double dx = x + 0.5 - hitbox.getCenterX();
          double dz = z + 0.5 - hitbox.getCenterZ();
          double distance = dx * dx + dz * dz;
          if (distance < nearest) {
            nearest = distance;
            ground = block;
          }
        }
      }
    }
    return ground;
  }

  // The ground under the player as if they stood at the given location, for a move they have not made yet.
  // Carpets and snow layers resolve to the block they lie on.
  @Nullable
  public static Block getSolidGround(@NotNull Player player, @NotNull Location at) {
    Vector offset = at.toVector().subtract(player.getLocation().toVector());
    Block ground = getGround(at.getWorld(), player.getBoundingBox().shift(offset));
    if (ground == null || ground.isSolid()) return ground;
    return ground.getRelative(BlockFace.DOWN);
  }

  private static int floor(double value) {
    return (int) Math.floor(value);
  }

}
