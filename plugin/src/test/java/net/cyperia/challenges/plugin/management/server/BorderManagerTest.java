package net.cyperia.challenges.plugin.management.server;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BorderManagerTest {

  private MockedStatic<Challenges> challenges;
  private MockedStatic<Bukkit> bukkit;
  private BorderManager border;
  private final World world = mock(World.class, RETURNS_DEEP_STUBS);

  @BeforeEach
  void setUp() {
    // update() walks the game worlds; deep stubs hand it an empty list
    challenges = mockStatic(Challenges.class, RETURNS_DEEP_STUBS);
    bukkit = mockStatic(Bukkit.class);
    bukkit.when(Bukkit::getMinecraftVersion).thenReturn("1.21.4");
    border = new BorderManager();
    when(world.getWorldBorder().getSize()).thenReturn(59_999_968d);
  }

  @AfterEach
  void tearDown() {
    challenges.close();
    bukkit.close();
  }

  @Test
  void withoutGrowthTheWorldKeepsItsOwnBorder() {
    assertFalse(border.isActive());
    assertEquals(59_999_968d, border.getSize(world));
  }

  @Test
  void growthsAddUpOnABorderThatStartsAtOneBlock() {
    AbstractChallenge level = challenge("level-border");
    AbstractChallenge damage = challenge("damage-border");

    border.setGrowth(level, 0, false);
    assertEquals(1, border.getSize(world));

    border.setGrowth(level, 4, false);
    border.setGrowth(damage, 3, false);
    assertEquals(8, border.getSize(world));

    border.clearGrowth(level);
    assertEquals(4, border.getSize(world));

    border.changeSize(2);
    assertEquals(6, border.getSize(world));
  }

  @Test
  void aFractionalSizeHoldsPlayersToTheWholeBlocksTheClientCollidesWith() {
    border.setCenter(world, new Location(world, 0.5, 64, 0.5));
    border.setGrowth(challenge("damage-border"), 4.1, false);

    // drawn from -2.05 to 3.05, walkable from -3 to 4
    assertFalse(border.isOutside(new Location(world, 3.7, 64, 0.5)));
    assertFalse(border.isOutside(new Location(world, -2.9, 64, 0.5)));
    assertTrue(border.isOutside(new Location(world, 4.2, 64, 0.5)));
    assertTrue(border.isOutside(new Location(world, 0.5, 64, -3.1)));
  }

  @Test
  void aPlayerOutsideIsPutJustInsideWhereTheyCrossed() {
    border.setCenter(world, new Location(world, 0.5, 64, 0.5));
    border.setGrowth(challenge("damage-border"), 9, false);

    Block feet = mock(Block.class);
    Block ground = mock(Block.class);
    when(feet.getY()).thenReturn(64);
    when(feet.isPassable()).thenReturn(true);
    when(feet.getRelative(BlockFace.UP)).thenReturn(feet);
    when(feet.getRelative(BlockFace.DOWN)).thenReturn(ground);
    when(world.getBlockAt(any(Location.class))).thenReturn(feet);

    Player player = mock(Player.class);
    when(player.getWorld()).thenReturn(world);
    when(player.getWidth()).thenReturn(0.6);
    when(player.getLocation()).thenReturn(new Location(world, 40, 64, 2));

    border.teleportInside(player);

    ArgumentCaptor<Location> target = ArgumentCaptor.forClass(Location.class);
    verify(player).teleport(target.capture());
    // a size of 10 around 0.5 is drawn up to 5.5, and half the player's width stays clear of it
    assertEquals(5.2, target.getValue().getX(), 1e-9);
    assertEquals(2, target.getValue().getZ(), 1e-9);
    assertEquals(64, target.getValue().getY(), 1e-9);
  }

  @Test
  void overTheVoidAPlayerIsPutOnTheCenterInstead() {
    border.setCenter(world, new Location(world, 0.5, 64, 0.5));
    border.setGrowth(challenge("damage-border"), 9, false);

    Block air = mock(Block.class);
    when(air.isPassable()).thenReturn(true);
    when(air.isEmpty()).thenReturn(true);
    when(air.getRelative(any(BlockFace.class))).thenReturn(air);
    when(world.getBlockAt(any(Location.class))).thenReturn(air);
    when(world.getMinHeight()).thenReturn(0);

    Player player = mock(Player.class);
    when(player.getWorld()).thenReturn(world);
    when(player.getWidth()).thenReturn(0.6);
    when(player.getLocation()).thenReturn(new Location(world, 40, 64, 2));

    border.teleportInside(player);

    ArgumentCaptor<Location> target = ArgumentCaptor.forClass(Location.class);
    verify(player).teleport(target.capture());
    assertEquals(0.5, target.getValue().getX(), 1e-9);
    assertEquals(0.5, target.getValue().getZ(), 1e-9);
  }

  private static AbstractChallenge challenge(String name) {
    AbstractChallenge challenge = mock(AbstractChallenge.class);
    when(challenge.getUniqueName()).thenReturn(name);
    return challenge;
  }

}
