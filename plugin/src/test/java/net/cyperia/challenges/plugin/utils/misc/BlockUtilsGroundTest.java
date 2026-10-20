package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockUtilsGroundTest {

  private static final Set<Material> SOLID = Set.of(Material.STONE, Material.DIRT, Material.OAK_FENCE);
  private static final BoundingBox FULL = new BoundingBox(0, 0, 0, 1, 1, 1);

  private final Map<List<Integer>, Block> blocks = new HashMap<>();
  private final World world = mock(World.class);
  private final Block empty = block(Material.AIR);

  BlockUtilsGroundTest() {
    when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenAnswer(call ->
      blocks.getOrDefault(List.of(call.<Integer>getArgument(0), call.<Integer>getArgument(1), call.<Integer>getArgument(2)), empty));
  }

  @Test
  void fullBlock_underThePlayer() {
    place(0, 64, 0, Material.STONE, FULL);
    assertEquals(Material.STONE, ground(0.5, 65, 0.5));
  }

  @Test
  void edge_countsTheBlockUnderTheHitboxWhenThePositionIsOverAir() {
    place(0, 64, 0, Material.STONE, FULL);
    assertEquals(Material.STONE, ground(1.2, 65, 0.5));
  }

  @Test
  void acrossTwoBlocks_theOneNearestTheCentreWins() {
    place(0, 64, 0, Material.STONE, FULL);
    place(1, 64, 0, Material.DIRT, FULL);
    assertEquals(Material.DIRT, ground(1.1, 65, 0.5));
  }

  @Test
  void carpet_winsOverTheBlockItLiesOn() {
    place(0, 64, 0, Material.STONE, FULL);
    place(0, 65, 0, Material.WHITE_CARPET, new BoundingBox(0, 0, 0, 1, 0.0625, 1));
    assertEquals(Material.WHITE_CARPET, ground(0.5, 65.0625, 0.5));
  }

  @Test
  void fence_isStoodOnFromTheCellAbove() {
    place(0, 64, 0, Material.OAK_FENCE, new BoundingBox(0.375, 0, 0.375, 0.625, 1.5, 0.625));
    assertEquals(Material.OAK_FENCE, ground(0.5, 65.5, 0.5));
  }

  @Test
  void blockWithoutHitbox_doesNotCount() {
    place(0, 65, 0, Material.TORCH);
    assertNull(ground(0.5, 65, 0.5));
  }

  @Test
  void midJump_isNoGround() {
    place(0, 64, 0, Material.STONE, FULL);
    assertNull(ground(0.5, 65.8, 0.5));
  }

  @Test
  void moveDestination_usesTheHitboxWhereThePlayerIsGoing() {
    place(3, 64, 0, Material.STONE, FULL);
    assertEquals(Material.STONE, solidGround(3.5, 65, 0.5));
  }

  @Test
  void solidGround_resolvesACarpetToTheBlockUnderIt() {
    place(0, 64, 0, Material.STONE, FULL);
    place(0, 65, 0, Material.WHITE_CARPET, new BoundingBox(0, 0, 0, 1, 0.0625, 1));
    assertEquals(Material.STONE, solidGround(0.5, 65.0625, 0.5));
  }

  private Material solidGround(double x, double feet, double z) {
    Player player = mock(Player.class);
    when(player.getLocation()).thenReturn(new Location(world, 0.5, 65, 0.5));
    when(player.getBoundingBox()).thenReturn(new BoundingBox(0.2, 65, 0.2, 0.8, 66.8, 0.8));
    Block ground = BlockUtils.getSolidGround(player, new Location(world, x, feet, z));
    return ground == null ? null : ground.getType();
  }

  private Material ground(double x, double feet, double z) {
    Block ground = BlockUtils.getGround(world, new BoundingBox(x - 0.3, feet, z - 0.3, x + 0.3, feet + 1.8, z + 0.3));
    return ground == null ? null : ground.getType();
  }

  private void place(int x, int y, int z, Material material, BoundingBox... shape) {
    Block block = block(material, shape);
    when(block.getRelative(BlockFace.DOWN)).thenAnswer(call -> blocks.getOrDefault(List.of(x, y - 1, z), empty));
    blocks.put(List.of(x, y, z), block);
  }

  private static Block block(Material material, BoundingBox... shape) {
    VoxelShape collision = mock(VoxelShape.class);
    when(collision.overlaps(any(BoundingBox.class))).thenAnswer(call -> {
      BoundingBox probe = call.getArgument(0);
      for (BoundingBox box : shape) {
        if (box.overlaps(probe)) return true;
      }
      return false;
    });

    Block block = mock(Block.class);
    when(block.getType()).thenReturn(material);
    when(block.isSolid()).thenReturn(SOLID.contains(material));
    when(block.getCollisionShape()).thenReturn(collision);
    return block;
  }

}
