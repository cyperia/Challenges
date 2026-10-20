package net.cyperia.challenges.plugin.utils.bukkit.misc;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Each fill here is a run of "cells" no-op blocks on its own mocked chunk; the fill's filter logs its
// tag on every visited cell and its done callback logs "<tag>-done", so a test reads the visiting order
// straight off that log instead of asserting on block writes.
class ChunkFillQueueTest {

  private static final long FAR_DEADLINE = System.nanoTime() + 60_000_000_000L;

  private final ChunkFillQueue queue = new ChunkFillQueue();
  private final List<String> log = new ArrayList<>();

  @Test
  void ringMode_runsUrgentBeforeNear() {
    Chunk urgentChunk = chunk();
    Chunk nearChunk = chunk();
    submit(urgentChunk, "urgent", 10, true);
    submit(nearChunk, "near", 10, false);

    queue.drainUntil(FAR_DEADLINE, ring(nearChunk, 1), 3);

    assertEquals(tagged("urgent", 10, "near", 10), log);
  }

  @Test
  void ringMode_nearFillsRunAscendingRingToCompletion() {
    Chunk far = chunk();
    Chunk near = chunk();
    // Submitted with the farther ring first, so a correct sort - not queue order - decides who goes first.
    submit(far, "ring2", 100, false);
    submit(near, "ring1", 100, false);

    ToIntFunction<Chunk> ring = chunk -> chunk == near ? 1 : 2;
    queue.drainUntil(FAR_DEADLINE, ring, 3);

    assertEquals(tagged("ring1", 100, "ring2", 100), log);
  }

  @Test
  void ringMode_tiesKeepQueueOrder() {
    Chunk first = chunk();
    Chunk second = chunk();
    submit(first, "first", 10, false);
    submit(second, "second", 10, false);

    queue.drainUntil(FAR_DEADLINE, chunk -> 1, 3);

    assertEquals(tagged("first", 10, "second", 10), log);
  }

  @Test
  void ringMode_farFillsRoundRobinInterleaved() {
    Chunk far1 = chunk();
    Chunk far2 = chunk();
    submit(far1, "far1", 100, false);
    submit(far2, "far2", 70, false);

    queue.drainUntil(FAR_DEADLINE, chunk -> Integer.MAX_VALUE, 3);

    List<String> expected = new ArrayList<>();
    expected.addAll(repeat("far1", ChunkFillQueue.TIME_CHECK_INTERVAL));
    expected.addAll(repeat("far2", ChunkFillQueue.TIME_CHECK_INTERVAL));
    expected.addAll(repeat("far1", 100 - ChunkFillQueue.TIME_CHECK_INTERVAL));
    expected.add("far1-done");
    expected.addAll(repeat("far2", 70 - ChunkFillQueue.TIME_CHECK_INTERVAL));
    expected.add("far2-done");
    assertEquals(expected, log);
  }

  @Test
  void ringMode_ringEqualToRadiusRunsToCompletionLikeNear() {
    // Both cell counts exceed TIME_CHECK_INTERVAL: a far fill would interleave in slices, so a boundary
    // fill treated as far here would show up in the log before "first" finishes.
    submit(chunk(), "first", 100, false);
    submit(chunk(), "second", 100, false);

    queue.drainUntil(FAR_DEADLINE, chunk -> 3, 3);

    assertEquals(tagged("first", 100, "second", 100), log);
  }

  @Test
  void ringMode_skipsUnloadedChunks() {
    Chunk unloaded = mock(Chunk.class);
    when(unloaded.isLoaded()).thenReturn(false);
    submit(unloaded, "gone", 10, false);
    submit(chunk(), "here", 10, false);

    queue.drainUntil(FAR_DEADLINE, chunk -> Integer.MAX_VALUE, 3);

    assertEquals(tagged("here", 10), log);
    assertFalse(log.contains("gone"));
  }

  @Test
  void plainDrainUntil_staysFifo() {
    submit(chunk(), "first", 100, false);
    submit(chunk(), "second", 100, false);

    queue.drainUntil(FAR_DEADLINE);

    assertEquals(tagged("first", 100, "second", 100), log);
  }

  @Test
  void urgentDone_reentrantSubmit_runsEachDoneOnce() {
    Chunk secondChunk = chunk();
    int[] secondOrder = {0, 1, 2};
    Predicate<Block> secondFilter = block -> {
      log.add("second");
      return true;
    };

    submitUrgent(chunk(), "first", 5, () -> {
      log.add("first-done");
      queue.submit(secondChunk, 0, 0, mock(BlockData.class), secondFilter, block -> false, () -> secondOrder, true,
          () -> log.add("second-done"));
    });

    queue.drainUntil(FAR_DEADLINE, chunk -> Integer.MAX_VALUE, 3);

    assertEquals(1, Collections.frequency(log, "first-done"));
    assertEquals(1, Collections.frequency(log, "second-done"));
    assertEquals(tagged("first", 5, "second", 3), log);
  }

  @Test
  void forgetFromDone_removesQueuedBackgroundFillDuringRingDrain() {
    Chunk chunkA = chunk();
    Chunk chunkB = chunk();
    submitWithDone(chunkA, "a", 5, () -> {
      log.add("a-done");
      queue.forget(chunkB);
    });
    submit(chunkB, "b", 5, false);

    ToIntFunction<Chunk> ring = chunk -> chunk == chunkA ? 1 : 2;
    queue.drainUntil(FAR_DEADLINE, ring, 3);

    assertEquals(tagged("a", 5), log);
    assertFalse(log.contains("b"));
    assertFalse(log.contains("b-done"));
  }

  @Test
  void pastDeadline_placesNothingLosesNothing() {
    submit(chunk(), "urgent", 10, true);
    submit(chunk(), "first", 10, false);
    submit(chunk(), "second", 10, false);

    queue.drainUntil(System.nanoTime() - 1, chunk -> 1, 3);
    assertEquals(List.of(), log);

    queue.drainUntil(FAR_DEADLINE, chunk -> 1, 3);
    assertEquals(tagged("urgent", 10, "first", 10, "second", 10), log);
  }

  @Test
  void replacement_setsWhatItReturnsAndSkipsNull() {
    Chunk chunk = chunk();
    Block block = chunk.getBlock(0, 0, 0);
    BlockData data = mock(BlockData.class);
    int[] visits = {0};

    int[] order = new int[10];
    for (int i = 0; i < order.length; i++) order[i] = i;

    // Every other visited block keeps its state.
    queue.submit(chunk, 0, 0, visited -> visits[0]++ % 2 == 0 ? data : null, visited -> true, () -> order, false, () -> {});
    queue.drain();

    assertEquals(10, visits[0]);
    verify(block, times(5)).setBlockData(data, true);
  }

  @Test
  void orderByDistance_keepsOnlyTheGivenCellsNearestFirst() {
    int height = 4;
    // Cells are (x * 16 + z) * height + y: the far corner, the column under the player, one column over.
    int far = (15 * 16 + 15) * height;
    int under = 0;
    int beside = 16 * height;
    List<Location> players = List.of(new Location(null, 0.5, 0.5, 0.5));

    int[] order = ChunkFillQueue.orderByDistance(new int[]{far, beside, under}, players, 0, 0, 0, height);

    assertArrayEquals(new int[]{under, beside, far}, order);
  }

  private Chunk chunk() {
    Chunk chunk = mock(Chunk.class);
    when(chunk.isLoaded()).thenReturn(true);
    Block block = mock(Block.class);
    when(chunk.getBlock(anyInt(), anyInt(), anyInt())).thenReturn(block);
    return chunk;
  }

  private void submit(Chunk chunk, String tag, int cells, boolean urgent) {
    submitWithDone(chunk, tag, cells, urgent, () -> log.add(tag + "-done"));
  }

  private void submitUrgent(Chunk chunk, String tag, int cells, Runnable done) {
    submitWithDone(chunk, tag, cells, true, done);
  }

  private void submitWithDone(Chunk chunk, String tag, int cells, Runnable done) {
    submitWithDone(chunk, tag, cells, false, done);
  }

  private void submitWithDone(Chunk chunk, String tag, int cells, boolean urgent, Runnable done) {
    int[] order = new int[cells];
    for (int i = 0; i < cells; i++) order[i] = i;
    Predicate<Block> filter = block -> {
      log.add(tag);
      return true;
    };
    queue.submit(chunk, 0, 0, mock(BlockData.class), filter, block -> false, () -> order, urgent, done);
  }

  private static ToIntFunction<Chunk> ring(Chunk near, int nearRing) {
    return chunk -> chunk == near ? nearRing : Integer.MAX_VALUE;
  }

  private static List<String> tagged(Object... tagsAndCounts) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < tagsAndCounts.length; i += 2) {
      String tag = (String) tagsAndCounts[i];
      int count = (int) tagsAndCounts[i + 1];
      result.addAll(repeat(tag, count));
      result.add(tag + "-done");
    }
    return result;
  }

  private static List<String> repeat(String tag, int times) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < times; i++) result.add(tag);
    return result;
  }

}
