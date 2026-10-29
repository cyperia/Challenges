package net.cyperia.challenges.plugin.management.server;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.management.server.BorderManager.Containment;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.GsonDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BorderManagerTest {

  private static final double VANILLA = 59_999_968d;

  private MockedStatic<Challenges> challenges;
  private MockedStatic<Bukkit> bukkit;
  private BorderManager border;
  private final World world = mock(World.class, RETURNS_DEEP_STUBS);
  private final FakeBorder real = new FakeBorder(world);
  private final List<FakeBorder> drawn = new ArrayList<>();
  private final AbstractChallenge level = challenge("level-border");

  @BeforeEach
  void setUp() {
    challenges = mockStatic(Challenges.class, RETURNS_DEEP_STUBS);
    bukkit = mockStatic(Bukkit.class);
    bukkit.when(Bukkit::getMinecraftVersion).thenReturn("1.21.4");
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Location.class))).thenReturn(true);
    bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
    bukkit.when(Bukkit::createWorldBorder).thenAnswer(_ -> {
      FakeBorder border = new FakeBorder(null);
      drawn.add(border);
      return border;
    });
    when(Challenges.getInstance().getGameWorldStorage().getGameWorlds()).thenReturn(List.of(world));
    when(world.getWorldBorder()).thenReturn(real);
    when(world.getName()).thenReturn("world");
    when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
    when(world.getSpawnLocation()).thenAnswer(_ -> new Location(world, 12, 64, 7));
    border = new BorderManager();
  }

  @AfterEach
  void tearDown() {
    challenges.close();
    bukkit.close();
  }

  @Test
  void withNothingSetPlayersKeepTheWorldsOwnBorder() {
    border.update(0);

    assertFalse(border.isActive());
    assertTrue(drawn.isEmpty());
    assertEquals(VANILLA, border.getSize(world));
  }

  @Test
  void aGrowingBorderStartsAtOneBlockAroundTheSpawn() {
    border.setGrowth(level, 4, false);

    assertEquals(5, drawn().size);
    assertEquals(12.5, drawn().centerX);
    assertEquals(7.5, drawn().centerZ);
    assertEquals(0, drawn().warningDistance);
  }

  @Test
  void growthsAddUp() {
    AbstractChallenge damage = challenge("damage-border");

    border.setGrowth(level, 4, false);
    border.setGrowth(damage, 3, false);
    assertEquals(8, border.getSize(world));

    border.clearGrowth(level);
    assertEquals(4, border.getSize(world));

    border.changeSize(2);
    assertEquals(6, border.getSize(world));
  }

  @Test
  void aBorderSetWithTheCommandIsWhereAGrowingBorderStarts() {
    border.setCenter(world, new Location(world, 300.5, 0, 300.5));
    border.setSize(10, 0);

    border.setGrowth(level, 4, false);

    assertEquals(14, drawn().size);
    assertEquals(300.5, drawn().centerX);
    assertEquals(300.5, drawn().centerZ);
  }

  @Test
  void sizeChangesGoOnTopOfTheCommandsBorder() {
    border.setCenter(world, new Location(world, 300.5, 0, 300.5));
    border.setSize(10, 0);

    border.changeSize(1);
    assertEquals(11, drawn().size);
    assertEquals(20, drawn().lastTicks, 1);

    border.changeSize(1);
    assertEquals(12, drawn().size);
    assertEquals(300.5, drawn().centerX);
  }

  @Test
  void sizeChangesWithoutABorderWaitForOne() {
    border.changeSize(2);
    assertFalse(border.isActive());
    assertTrue(drawn.isEmpty());

    border.setSize(10, 0);
    assertEquals(10, drawn().size);
    border.changeSize(1);
    assertEquals(11, drawn().size);
  }

  @Test
  void aSizeSetWhileAChallengeGrowsTheBorderIsWhereItGrowsOnFrom() {
    border.setGrowth(level, 4, false);
    border.changeSize(2);
    assertEquals(7, border.getSize(world));

    border.setSize(10, 0);
    assertEquals(10, drawn().size);

    border.setGrowth(level, 6, false);
    assertEquals(12, drawn().size);
  }

  @Test
  void sizeChangesStopAtOneBlock() {
    border.setSize(3, 0);
    border.changeSize(-10);
    assertEquals(1, drawn().size);
  }

  @Test
  void aNewBorderAppearsAtItsSizeInsteadOfSlidingInFromTheHorizon() {
    border.setSize(10, 5);
    assertEquals(10, drawn().size);
    assertEquals(0, drawn().lastTicks);

    border.setSize(20, 5);
    assertEquals(20, drawn().size);
    assertEquals(100, drawn().lastTicks, 1);
  }

  @Test
  void theWorldsOwnBorderIsNeverWritten() {
    border.setCenter(world, new Location(world, 300.5, 0, 300.5));
    border.setSize(10, 0);
    border.setGrowth(level, 4, false);
    border.changeSize(3);
    border.reset();

    assertEquals(0, real.writes);
  }

  @Test
  void aResetLeavesOnlyWhatChallengesGrow() {
    border.setSize(10, 0);
    border.reset();
    assertFalse(border.isActive());

    border.setSize(10, 0);
    border.setGrowth(level, 4, false);
    border.reset();
    assertTrue(border.isActive());
    assertEquals(5, border.getSize(world));
  }

  @Test
  void theBorderOutlivesARestart() {
    border.setCenter(world, new Location(world, 300.5, 0, -20.5));
    border.setSize(10, 0);
    border.changeSize(2);
    border.setDamage(2, 0.5);
    border.setContainment(Containment.PUSH);
    Document save = new GsonDocument();
    border.writeGameState(save);

    BorderManager restarted = new BorderManager();
    restarted.loadGameState(save);

    assertEquals(12, restarted.getSize(world));
    assertEquals(300.5, restarted.getCenter(world).getX());
    assertEquals(-20.5, restarted.getCenter(world).getZ());
    assertEquals(2, restarted.getDamageBuffer());
    assertEquals(0.5, restarted.getDamagePerBlock());
    assertEquals(Containment.PUSH, restarted.getContainment());
  }

  @Test
  void aSaveWithoutASizeHasNoBorder() {
    border.changeSize(2);
    Document save = new GsonDocument();
    border.writeGameState(save);

    BorderManager restarted = new BorderManager();
    restarted.loadGameState(save);

    assertFalse(restarted.isActive());
    assertEquals(Containment.DAMAGE, restarted.getContainment());
  }

  @Test
  void aNetherBorderIsCenteredAtEightTimesItsCoordinates() {
    World nether = mock(World.class, RETURNS_DEEP_STUBS);
    when(nether.getEnvironment()).thenReturn(World.Environment.NETHER);
    when(Challenges.getInstance().getGameWorldStorage().getGameWorlds()).thenReturn(List.of(nether));

    border.setCenter(nether, new Location(nether, 10, 70, -3));
    border.setSize(10, 0);

    assertEquals(80, drawn().centerX);
    assertEquals(-24, drawn().centerZ);
  }

  @Test
  void theDistanceOutsideIsMeasuredFromWhereTheClientStopsWalking() {
    border.setCenter(world, new Location(world, 0.5, 0, 0.5));
    border.setSize(9, 0);

    // drawn from -4 to 5, walkable from -4 to 5; at 10 it is drawn from -4.5 to 5.5 and walkable from -5 to 6
    assertEquals(0, border.distanceOutside(new Location(world, 4.7, 64, 0)));
    assertEquals(4, border.distanceOutside(new Location(world, 9, 64, 0)), 1e-9);
    assertEquals(7, border.distanceOutside(new Location(world, 2, 64, -11)), 1e-9);

    border.setSize(10, 0);
    assertEquals(0, border.distanceOutside(new Location(world, 5.7, 64, 0)));
    assertEquals(0, border.distanceOutside(new Location(world, -4.7, 64, 0)));
    assertEquals(0.5, border.distanceOutside(new Location(world, 6.5, 64, 0)), 1e-9);
  }

  @Test
  void aSlidingBorderHoldsPlayersToWhereItIsDrawnNow() {
    border.setCenter(world, new Location(world, 0.5, 0, 0.5));
    border.setSize(20, 0);
    border.setSize(4, 60);

    assertEquals(4, border.getSize(world));
    assertEquals(20, border.getDrawnSize(world), 0.1);
    assertFalse(border.isOutside(new Location(world, 8.5, 64, 0.5)));
    assertEquals(0, border.distanceOutside(new Location(world, 8.5, 64, 0.5)));
  }

  @Test
  void aBorderUsedBeforeAResetStartsAtItsNewSize() {
    border.setSize(5000, 0);
    border.reset();
    border.setSize(100, 10);

    assertEquals(100, drawn().size);
    assertEquals(0, drawn().lastTicks);
    assertEquals(100, border.getDrawnSize(world));
  }

  @Test
  void aCenterMovedDuringASlideKeepsItSliding() {
    border.setSize(20, 0);
    border.setSize(4, 60);
    border.setCenter(world, new Location(world, 50, 0, 50));

    assertEquals(4, drawn().size);
    assertEquals(1200, drawn().lastTicks, 2);
    assertEquals(50, drawn().centerX);
  }

  @Test
  void aBorderWithoutASlideIsDrawnAtItsSize() {
    border.setSize(20, 0);
    border.setSize(4, 0);

    assertEquals(4, border.getDrawnSize(world));
  }

  @Test
  void aFractionalSizeHoldsPlayersToTheWholeBlocksTheClientCollidesWith() {
    border.setCenter(world, new Location(world, 0.5, 0, 0.5));
    border.setGrowth(challenge("damage-border"), 4.1, false);

    // drawn from -2.05 to 3.05, walkable from -3 to 4
    assertFalse(border.isOutside(new Location(world, 3.7, 64, 0.5)));
    assertFalse(border.isOutside(new Location(world, -2.9, 64, 0.5)));
    assertTrue(border.isOutside(new Location(world, 4.2, 64, 0.5)));
    assertTrue(border.isOutside(new Location(world, 0.5, 64, -3.1)));
  }

  @Test
  void aPlayerOutsideIsPutJustInsideWhereTheyCrossed() {
    border.setCenter(world, new Location(world, 0.5, 0, 0.5));
    border.setGrowth(challenge("damage-border"), 9, false);

    Block feet = mock(Block.class);
    Block ground = mock(Block.class);
    when(feet.getY()).thenReturn(64);
    when(feet.isPassable()).thenReturn(true);
    when(feet.getRelative(BlockFace.UP)).thenReturn(feet);
    when(feet.getRelative(BlockFace.DOWN)).thenReturn(ground);
    when(world.getBlockAt(any(Location.class))).thenReturn(feet);

    Player player = player();
    border.teleportInside(player);

    ArgumentCaptor<Location> target = ArgumentCaptor.forClass(Location.class);
    verify(player).teleportAsync(target.capture());
    // a size of 10 around 0.5 is drawn up to 5.5, and half the player's width stays clear of it
    assertEquals(5.2, target.getValue().getX(), 1e-9);
    assertEquals(2, target.getValue().getZ(), 1e-9);
    assertEquals(64, target.getValue().getY(), 1e-9);
  }

  @Test
  void aPlayerWhoseTeleportIsStillOnItsWayIsNotSentAgain() {
    border.setCenter(world, new Location(world, 0.5, 0, 0.5));
    border.setGrowth(challenge("damage-border"), 9, false);
    Block feet = mock(Block.class);
    when(feet.isPassable()).thenReturn(true);
    when(feet.getRelative(any(BlockFace.class))).thenReturn(feet);
    when(world.getBlockAt(any(Location.class))).thenReturn(feet);

    Player player = player();
    CompletableFuture<Boolean> pending = new CompletableFuture<>();
    when(player.teleportAsync(any(Location.class))).thenReturn(pending);
    border.teleportInside(player);
    border.teleportInside(player);
    verify(player, times(1)).teleportAsync(any(Location.class));

    pending.complete(true);
    border.teleportInside(player);
    verify(player, times(2)).teleportAsync(any(Location.class));
  }

  @Test
  void overTheVoidAPlayerIsPutOnTheCenterInstead() {
    border.setCenter(world, new Location(world, 0.5, 0, 0.5));
    border.setGrowth(challenge("damage-border"), 9, false);

    Block air = mock(Block.class);
    when(air.isPassable()).thenReturn(true);
    when(air.isEmpty()).thenReturn(true);
    when(air.getRelative(any(BlockFace.class))).thenReturn(air);
    when(world.getBlockAt(any(Location.class))).thenReturn(air);
    when(world.getMinHeight()).thenReturn(0);

    Player player = player();
    border.teleportInside(player);

    ArgumentCaptor<Location> target = ArgumentCaptor.forClass(Location.class);
    verify(player).teleportAsync(target.capture());
    assertEquals(0.5, target.getValue().getX(), 1e-9);
    assertEquals(0.5, target.getValue().getZ(), 1e-9);
  }

  private FakeBorder drawn() {
    assertEquals(1, drawn.size(), "one border per world");
    return drawn.getFirst();
  }

  private Player player() {
    Player player = mock(Player.class);
    when(player.getWorld()).thenReturn(world);
    when(player.getWidth()).thenReturn(0.6);
    when(player.getLocation()).thenReturn(new Location(world, 40, 64, 2));
    when(player.getUniqueId()).thenReturn(UUID.randomUUID());
    when(player.teleportAsync(any(Location.class))).thenReturn(CompletableFuture.completedFuture(true));
    return player;
  }

  private static AbstractChallenge challenge(String name) {
    AbstractChallenge challenge = mock(AbstractChallenge.class);
    when(challenge.getUniqueName()).thenReturn(name);
    return challenge;
  }

  private static final class FakeBorder implements WorldBorder {

    private final World world;
    private double size = VANILLA;
    private double centerX;
    private double centerZ;
    private int warningDistance = 5;
    private int warningTicks = 300;
    private long lastTicks;
    private int writes;

    FakeBorder(@Nullable World world) {
      this.world = world;
    }

    @Override
    public @Nullable World getWorld() {
      return world;
    }

    @Override
    public void reset() {
      size = VANILLA;
      centerX = 0;
      centerZ = 0;
    }

    @Override
    public double getSize() {
      return size;
    }

    @Override
    public void setSize(double newSize) {
      changeSize(newSize, 0);
    }

    @Override
    public void changeSize(double newSize, long ticks) {
      writes++;
      size = newSize;
      lastTicks = ticks;
    }

    @Override
    public @NotNull Location getCenter() {
      return new Location(world, centerX, 0, centerZ);
    }

    @Override
    public void setCenter(double x, double z) {
      writes++;
      centerX = x;
      centerZ = z;
    }

    @Override
    public void setCenter(@NotNull Location location) {
      setCenter(location.getX(), location.getZ());
    }

    @Override
    public double getDamageBuffer() {
      return 5;
    }

    @Override
    public void setDamageBuffer(double blocks) {
    }

    @Override
    public double getDamageAmount() {
      return 0.2;
    }

    @Override
    public void setDamageAmount(double damage) {
    }

    @Override
    public int getWarningTimeTicks() {
      return warningTicks;
    }

    @Override
    public void setWarningTimeTicks(int ticks) {
      warningTicks = ticks;
    }

    @Override
    public int getWarningDistance() {
      return warningDistance;
    }

    @Override
    public void setWarningDistance(int distance) {
      warningDistance = distance;
    }

    @Override
    public boolean isInside(@NotNull Location location) {
      return true;
    }

    @Override
    public double getMaxSize() {
      return VANILLA;
    }

    @Override
    public double getMaxCenterCoordinate() {
      return 29_999_984d;
    }

  }

}
