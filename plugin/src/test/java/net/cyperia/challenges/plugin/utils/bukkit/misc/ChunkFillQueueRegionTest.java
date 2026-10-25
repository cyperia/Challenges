package net.cyperia.challenges.plugin.utils.bukkit.misc;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// The draining thread owns no chunk here, as the global region on a regionised server: slices are handed
// to the chunk's region, which the test runs by hand
class ChunkFillQueueRegionTest {

  private final Deque<Runnable> regionTasks = new ArrayDeque<>();
  private final List<String> log = new ArrayList<>();
  private final ChunkFillQueue queue = new ChunkFillQueue(new ChunkFillQueue.Owner() {
    @Override
    public boolean owns(@NotNull Chunk chunk) {
      return false;
    }

    @Override
    public void run(@NotNull Chunk chunk, @NotNull Runnable task) {
      regionTasks.add(task);
    }
  });

  @Test
  void drain_placesNothingItself() {
    queue.submit(chunk(), 0, 0, mock(BlockData.class), block -> log.add("visited"), block -> false, () -> new int[]{0, 1, 2}, false, () -> {});

    queue.drain();

    assertEquals(List.of(), log);
    assertEquals(1, regionTasks.size());
  }

  @Test
  void slice_runsOnTheRegionAndFinishesWithDone() {
    submit(chunk(), "a", 3, false);
    queue.drain();

    regionTasks.poll().run();

    assertEquals(List.of("a", "a", "a", "a-done"), log);
  }

  @Test
  void partialSlice_isQueuedAgainForTheNextDrain() {
    submit(chunk(), "a", 3, false);
    queue.drain(2);
    regionTasks.poll().run();
    assertEquals(List.of("a", "a"), log);

    queue.drain(2);
    regionTasks.poll().run();

    assertEquals(List.of("a", "a", "a", "a-done"), log);
  }

  @Test
  void fillInFlight_isNotHandedOutTwice() {
    submit(chunk(), "a", 3, false);

    queue.drain();
    queue.drain();

    assertEquals(1, regionTasks.size());
  }

  @Test
  void forgetWhileInFlight_dropsTheFillWithoutDone() {
    Chunk chunk = chunk();
    submit(chunk, "a", 3, false);
    queue.drain();

    queue.forget(chunk);
    regionTasks.poll().run();

    assertFalse(log.contains("a-done"));
    queue.drain();
    assertEquals(0, regionTasks.size());
  }

  @Test
  void oneDrain_handsOutAtMostEightSlices() {
    for (int i = 0; i < 12; i++) submit(chunk(), "fill" + i, 1, false);

    queue.drainUntil(System.nanoTime() + 60_000_000_000L);

    assertEquals(8, regionTasks.size());
  }

  @Test
  void handedOffSlice_stopsAfterItsShareOfTheDrainTime() {
    Chunk chunk = chunk();
    int[] order = new int[1000];
    for (int i = 0; i < order.length; i++) order[i] = i;
    queue.submit(chunk, 0, 0, mock(BlockData.class), block -> {
      log.add("cell");
      try {
        Thread.sleep(1);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      return true;
    }, block -> false, () -> order, false, () -> log.add("done"));

    queue.drainUntil(System.nanoTime() + 80_000_000L);
    regionTasks.poll().run();

    assertTrue(log.size() < 200);
    assertFalse(log.contains("done"));
    queue.drainUntil(System.nanoTime() + 80_000_000L);
    assertEquals(1, regionTasks.size());
  }

  private void submit(Chunk chunk, String tag, int cells, boolean urgent) {
    int[] order = new int[cells];
    for (int i = 0; i < cells; i++) order[i] = i;
    queue.submit(chunk, 0, 0, mock(BlockData.class), block -> {
      log.add(tag);
      return true;
    }, block -> false, () -> order, urgent, () -> log.add(tag + "-done"));
  }

  private static Chunk chunk() {
    Chunk chunk = mock(Chunk.class);
    when(chunk.isLoaded()).thenReturn(true);
    when(chunk.getBlock(anyInt(), anyInt(), anyInt())).thenReturn(mock(Block.class));
    return chunk;
  }

}
