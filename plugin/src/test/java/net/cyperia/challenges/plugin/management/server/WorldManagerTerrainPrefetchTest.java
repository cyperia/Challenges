package net.cyperia.challenges.plugin.management.server;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.document.GsonDocument;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WorldManagerTerrainPrefetchTest {

  private MockedStatic<Challenges> challenges;
  private MockedStatic<Logger> logger;
  private WorldManager manager;
  private final Location at = mock(Location.class);

  @BeforeEach
  void setUp() {
    challenges = mockStatic(Challenges.class, RETURNS_DEEP_STUBS);
    challenges.when(() -> Challenges.getInstance().getConfigDocument()).thenReturn(new GsonDocument());
    logger = mockStatic(Logger.class);
    manager = new WorldManager();
  }

  @AfterEach
  void tearDown() {
    logger.close();
    challenges.close();
  }

  @Test
  void completesImmediatelyWithoutAPrefetcher() {
    assertTrue(manager.prefetchTerrain(at).isDone());
  }

  @Test
  void delegatesToTheInstalledPrefetcher() {
    CompletableFuture<Void> pending = new CompletableFuture<>();
    manager.setTerrainPrefetcher(location -> {
      assertSame(at, location);
      return pending;
    });

    CompletableFuture<Void> result = manager.prefetchTerrain(at);

    assertFalse(result.isDone());
    pending.complete(null);
    assertTrue(result.isDone());
  }

  @Test
  void rejectsNull() {
    assertThrows(NullPointerException.class, () -> manager.setTerrainPrefetcher(null));
  }

  @Test
  void anExceptionalFutureStillCompletes() {
    manager.setTerrainPrefetcher(location -> CompletableFuture.failedFuture(new IllegalStateException("boom")));

    CompletableFuture<Void> result = manager.prefetchTerrain(at);

    assertTrue(result.isDone());
    assertFalse(result.isCompletedExceptionally());
  }

  @Test
  void aThrowingPrefetcherStillCompletes() {
    manager.setTerrainPrefetcher(location -> {
      throw new NoSuchMethodError("prefetch");
    });

    CompletableFuture<Void> result = manager.prefetchTerrain(at);

    assertTrue(result.isDone());
    assertFalse(result.isCompletedExceptionally());
  }

  @Test
  void aNullFutureStillCompletes() {
    manager.setTerrainPrefetcher(location -> null);

    assertTrue(manager.prefetchTerrain(at).isDone());
  }

  @Test
  void loadsTheChunkOnlyAfterThePrefetchCompleted() {
    World world = mock(World.class);
    Chunk chunk = mock(Chunk.class);
    when(at.getWorld()).thenReturn(world);
    when(world.getChunkAtAsync(at)).thenReturn(CompletableFuture.completedFuture(chunk));
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(at)).thenReturn(true);
      CompletableFuture<Void> pending = new CompletableFuture<>();
      manager.setTerrainPrefetcher(location -> pending);
      AtomicReference<Chunk> received = new AtomicReference<>();

      CompletableFuture<Void> done = manager.loadChunkAfterPrefetch(at, received::set);

      verify(world, never()).getChunkAtAsync(any(Location.class));
      assertFalse(done.isDone());
      pending.complete(null);
      verify(world).getChunkAtAsync(at);
      assertSame(chunk, received.get());
      assertTrue(done.isDone());
    }
  }

  @Test
  void aFailedChunkLoadFailsTheFuture() {
    World world = mock(World.class);
    when(at.getWorld()).thenReturn(world);
    when(world.getChunkAtAsync(at)).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("boom")));
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(at)).thenReturn(true);

      assertTrue(manager.loadChunkAfterPrefetch(at, chunk -> { }).isCompletedExceptionally());
    }
  }

}
