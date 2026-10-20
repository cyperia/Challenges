package net.cyperia.challenges.plugin.challenges.type.abstraction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorldGeneratorPortalTest {

  @Test
  void netherPortal_keepsTheScaledCoordinatesInTheGeneratedWorld() {
    World generatedNether = world(Environment.NETHER);
    Location to = new Location(world(Environment.NETHER), 12.5, 70, -3);

    Location redirected = WorldGeneratorChallenge.redirectPortalTravel(to, generatedNether, false, true);

    assertSame(generatedNether, redirected.getWorld());
    assertEquals(12.5, redirected.getX());
    assertEquals(70, redirected.getY());
    assertEquals(-3, redirected.getZ());
  }

  @Test
  void leavingTheEnd_landsInTheMiddleOfTheGeneratedOverworldSpawnBlock() {
    World generatedOverworld = world(Environment.NORMAL);
    when(generatedOverworld.getSpawnLocation()).thenReturn(new Location(generatedOverworld, 2, 60, -3, 90, 0));

    Location redirected = WorldGeneratorChallenge.redirectPortalTravel(new Location(world(Environment.NORMAL), 200, 80, 200), generatedOverworld, true, true);

    assertSame(generatedOverworld, redirected.getWorld());
    assertEquals(2.5, redirected.getX());
    assertEquals(60, redirected.getY());
    assertEquals(-2.5, redirected.getZ());
    assertEquals(90, redirected.getYaw());
  }

  @Test
  void enteringTheEnd_buildsThePlatformAndPutsPlayersOnIt() {
    World generatedEnd = world(Environment.THE_END);
    Block block = mock(Block.class);
    when(block.getType()).thenReturn(Material.AIR);
    when(generatedEnd.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(block);
    Block floor = mock(Block.class);
    when(floor.getType()).thenReturn(Material.AIR);
    when(generatedEnd.getBlockAt(100, 48, 0)).thenReturn(floor);

    Location player = WorldGeneratorChallenge.redirectPortalTravel(new Location(world(Environment.THE_END), 0, 0, 0), generatedEnd, true, true);
    Location entity = WorldGeneratorChallenge.redirectPortalTravel(new Location(world(Environment.THE_END), 0, 0, 0), generatedEnd, true, false);

    verify(floor, times(2)).setType(Material.OBSIDIAN, false);
    assertEquals(new Location(generatedEnd, 100.5, 49, 0.5, 90, 0), player);
    assertEquals(new Location(generatedEnd, 100.5, 50, 0.5, 90, 0), entity);
  }

  private static World world(Environment environment) {
    World world = mock(World.class);
    when(world.getEnvironment()).thenReturn(environment);
    return world;
  }

}
