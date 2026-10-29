package net.cyperia.commons.bukkit.utils.misc;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegionsTest {

  private final Plugin plugin = mock(Plugin.class);
  private final GlobalRegionScheduler global = mock(GlobalRegionScheduler.class);
  private final RegionScheduler region = mock(RegionScheduler.class);
  private final AsyncScheduler async = mock(AsyncScheduler.class);
  private final Entity entity = mock(Entity.class);
  private final EntityScheduler entityScheduler = mock(EntityScheduler.class);
  private final Location location = mock(Location.class);
  private MockedStatic<Bukkit> bukkit;

  @BeforeEach
  void mockServer() {
    bukkit = org.mockito.Mockito.mockStatic(Bukkit.class);
    bukkit.when(Bukkit::getGlobalRegionScheduler).thenReturn(global);
    bukkit.when(Bukkit::getRegionScheduler).thenReturn(region);
    bukkit.when(Bukkit::getAsyncScheduler).thenReturn(async);
    when(entity.getScheduler()).thenReturn(entityScheduler);
    when(plugin.isEnabled()).thenReturn(true);
  }

  @AfterEach
  void closeServer() {
    bukkit.close();
    Regions.regionized = () -> false;
  }

  @Test
  void global_runsInlineOnTheGlobalThread() {
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(true);
    AtomicInteger runs = new AtomicInteger();

    Regions.global(plugin, runs::incrementAndGet);

    assertEquals(1, runs.get());
    verify(global, never()).execute(any(), any());
  }

  @Test
  void global_isScheduledFromAnyOtherThread() {
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(false);
    Runnable task = () -> {
    };

    Regions.global(plugin, task);

    verify(global).execute(plugin, task);
  }

  @Test
  void entity_runsInlineWhenTheThreadOwnsIt() {
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(true);
    AtomicInteger runs = new AtomicInteger();

    Regions.entity(plugin, entity, runs::incrementAndGet);

    assertEquals(1, runs.get());
  }

  @Test
  void entity_isScheduledOnTheEntityOtherwise() {
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(false);
    when(entityScheduler.execute(any(), any(), any(), anyLong())).thenReturn(true);
    Runnable task = () -> {
    };

    Regions.entity(plugin, entity, task);

    verify(entityScheduler).execute(plugin, task, null, 1);
  }

  @Test
  void entity_runsTheRetiredFallbackWhenTheEntityIsGone() {
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(false);
    when(entityScheduler.execute(any(), any(), any(), anyLong())).thenReturn(false);
    AtomicInteger retired = new AtomicInteger();

    Regions.entity(plugin, entity, () -> {
    }, retired::incrementAndGet);

    assertEquals(1, retired.get());
  }

  @Test
  void at_isScheduledOnTheRegionOfTheLocationOtherwise() {
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(location)).thenReturn(false);
    Runnable task = () -> {
    };

    Regions.at(plugin, location, task);

    verify(region).execute(plugin, location, task);
  }

  @Test
  void timers_neverStartBeforeTheFirstTick() {
    Regions.timer(plugin, _ -> {
    }, 0, 20);
    Regions.asyncTimer(plugin, _ -> {
    }, 0, 20);

    verify(global).runAtFixedRate(eq(plugin), any(), eq(1L), eq(20L));
    verify(async).runAtFixedRate(eq(plugin), any(), eq(50L), eq(1000L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void later_withoutDelayRunsOnTheNextTick() {
    Regions.later(plugin, () -> {
    }, 0);

    verify(global).run(eq(plugin), any());
  }

  @Test
  void disabledPlugin_dropsTasksForTargetsTheThreadDoesNotOwn() {
    when(plugin.isEnabled()).thenReturn(false);
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(false);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(false);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(location)).thenReturn(false);
    AtomicInteger runs = new AtomicInteger();
    AtomicInteger retired = new AtomicInteger();

    Regions.global(plugin, runs::incrementAndGet);
    Regions.entity(plugin, entity, runs::incrementAndGet, retired::incrementAndGet);
    Regions.at(plugin, location, runs::incrementAndGet);

    assertEquals(0, runs.get());
    assertEquals(1, retired.get());
    verify(global, never()).execute(any(), any());
    verify(entityScheduler, never()).execute(any(), any(), any(), anyLong());
    verify(region, never()).execute(any(), any(), any());
  }

  @Test
  void disabledPlugin_runsTasksForTargetsTheThreadOwns() {
    when(plugin.isEnabled()).thenReturn(false);
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(false);
    bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
    bukkit.when(Bukkit::isStopping).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(location)).thenReturn(true);
    AtomicInteger runs = new AtomicInteger();

    Regions.global(plugin, runs::incrementAndGet);
    Regions.entity(plugin, entity, runs::incrementAndGet);
    Regions.at(plugin, location, runs::incrementAndGet);

    assertEquals(3, runs.get());
  }

  private Player playerIn(World world, Location location, EntityScheduler scheduler) {
    Player player = mock(Player.class);
    when(player.getWorld()).thenReturn(world);
    when(player.getLocation()).thenReturn(location);
    when(player.getScheduler()).thenReturn(scheduler);
    return player;
  }

  @Test
  void forEachRegion_scansOnceWhenAllPlayersShareTheRegion() {
    World world = mock(World.class);
    Player first = playerIn(world, location, entityScheduler);
    Player second = playerIn(world, location, entityScheduler);
    bukkit.when(Bukkit::getOnlinePlayers).thenAnswer(_ -> List.of(first, second));
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Entity.class))).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Location.class))).thenReturn(true);
    AtomicInteger scans = new AtomicInteger();

    CompletableFuture<Void> done = Regions.forEachRegion(plugin, world, scans::incrementAndGet);

    assertEquals(1, scans.get());
    assertTrue(done.isDone());
  }

  @Test
  void forEachRegion_scansEveryRegionOnceAndCompletesAfterTheLast() {
    World world = mock(World.class);
    Map<Location, String> regionOf = new HashMap<>();
    AtomicReference<String> current = new AtomicReference<>();
    List<Player> players = new ArrayList<>();
    for (String region : List.of("a", "b", "a")) {
      Location at = mock(Location.class);
      regionOf.put(at, region);
      EntityScheduler scheduler = mock(EntityScheduler.class);
      when(scheduler.execute(any(), any(), any(), anyLong())).thenAnswer(call -> {
        current.set(region);
        ((Runnable) call.getArgument(1)).run();
        return true;
      });
      players.add(playerIn(world, at, scheduler));
    }
    bukkit.when(Bukkit::getOnlinePlayers).thenAnswer(_ -> players);
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Entity.class))).thenReturn(false);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Location.class)))
      .thenAnswer(call -> regionOf.get(call.<Location>getArgument(0)).equals(current.get()));
    AtomicInteger scans = new AtomicInteger();

    CompletableFuture<Void> done = Regions.forEachRegion(plugin, world, scans::incrementAndGet);

    assertEquals(2, scans.get());
    assertTrue(done.isDone());
  }

  @Test
  void forEachRegion_countsAPlayerWhoLeftBeforeTheTaskRan() {
    World world = mock(World.class);
    Player gone = playerIn(world, location, entityScheduler);
    bukkit.when(Bukkit::getOnlinePlayers).thenAnswer(_ -> List.of(gone));
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Entity.class))).thenReturn(false);
    when(entityScheduler.execute(any(), any(), any(), anyLong())).thenReturn(false);
    AtomicInteger scans = new AtomicInteger();

    CompletableFuture<Void> done = Regions.forEachRegion(plugin, world, scans::incrementAndGet);

    assertEquals(0, scans.get());
    assertTrue(done.isDone());
  }

  @Test
  void forEachRegion_scansInlineOnceTheWorldHasNoPlayersOutsideRegionizedServers() {
    World world = mock(World.class);
    bukkit.when(Bukkit::getOnlinePlayers).thenAnswer(_ -> List.of());
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(true);
    AtomicInteger scans = new AtomicInteger();

    CompletableFuture<Void> done = Regions.forEachRegion(plugin, world, scans::incrementAndGet);

    assertEquals(1, scans.get());
    assertTrue(done.isDone());
  }

  @Test
  void forEachRegion_scansInlineWhenThePluginIsDisabledOnShutdown() {
    when(plugin.isEnabled()).thenReturn(false);
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(false);
    bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
    bukkit.when(Bukkit::isStopping).thenReturn(true);
    AtomicInteger scans = new AtomicInteger();

    CompletableFuture<Void> done = Regions.forEachRegion(plugin, mock(World.class), scans::incrementAndGet);

    assertEquals(1, scans.get());
    assertTrue(done.isDone());
  }


  @Test
  void remove_onlyMarksNonPersistentWhileARegionisedServerStops() {
    Regions.regionized = () -> true;
    bukkit.when(Bukkit::isStopping).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(true);

    Regions.remove(plugin, entity);

    verify(entity).setPersistent(false);
    verify(entity, never()).remove();
  }

  @Test
  void remove_removesInlineWhileANonRegionisedServerStops() {
    bukkit.when(Bukkit::isStopping).thenReturn(true);
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(true);
    when(entity.isValid()).thenReturn(true);

    Regions.remove(plugin, entity);

    verify(entity).remove();
  }

  @Test
  void remove_skipsAnEntityThatIsAlreadyGone() {
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(true);
    when(entity.isValid()).thenReturn(false);

    Regions.remove(plugin, entity);

    verify(entity, never()).remove();
  }

  @Test
  void remove_isScheduledOnTheEntityFromAnotherThread() {
    Regions.regionized = () -> true;
    bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(entity)).thenReturn(false);
    when(entityScheduler.execute(any(), any(), any(), anyLong())).thenReturn(true);

    Regions.remove(plugin, entity);

    verify(entityScheduler).execute(eq(plugin), any(), eq(null), eq(1L));
    verify(entity, never()).remove();
  }
}
