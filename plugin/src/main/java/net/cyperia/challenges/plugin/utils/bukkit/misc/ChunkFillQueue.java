package net.cyperia.challenges.plugin.utils.bukkit.misc;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Replaces matching blocks of whole chunks a few columns per tick. A chunk holds up to ~100k blocks,
 * which is a visible freeze when done in one tick, so callers submit the work and drain it from a
 * one tick task instead.
 */
public final class ChunkFillQueue {

  /** Blocks to visit per tick. A full height overworld column is 384 blocks, so this is 16 of them. */
  public static final int DEFAULT_BUDGET = 6144;

  private static final int COLUMNS = 16 * 16;
  static final int TIME_CHECK_INTERVAL = 64;

  private final Deque<Fill> urgent = new ArrayDeque<>();
  private final Deque<Fill> queue = new ArrayDeque<>();

  /**
   * Queues every block between {@code fromY} and {@code toY} (both inclusive) that the filter accepts
   * to be replaced with {@code material}.
   */
  public void submit(@NotNull Chunk chunk, int fromY, int toY, @NotNull Material material, @NotNull Predicate<Block> filter) {
    submit(chunk, fromY, toY, material.createBlockData(), filter);
  }

  /**
   * Same as {@link #submit(Chunk, int, int, Material, Predicate)}, but for a caller that needs a
   * specific block state rather than a material's default, such as leaves forced {@code persistent} so
   * they do not decay once the fill has placed them.
   */
  public void submit(@NotNull Chunk chunk, int fromY, int toY, @NotNull BlockData data, @NotNull Predicate<Block> filter) {
    submit(chunk, fromY, toY, data, filter, block -> false, () -> shuffledColumns(toY - fromY + 1), false, () -> {});
  }

  /**
   * Same as {@link #submit(Chunk, int, int, BlockData, Predicate)}, visiting only the blocks in
   * {@code order}, in that order. An entry is {@code (x * 16 + z) * height + (y - fromY)}. An urgent fill
   * goes before every other one, even one already under way, and the latest urgent fill goes first: it is
   * the chunk a player just entered. {@code done} runs once the fill is through;
   * a fill that is forgotten, or whose chunk unloads, never runs it. Blocks {@code physics} accepts are
   * placed with block updates.
   */
  public void submit(@NotNull Chunk chunk, int fromY, int toY, @NotNull BlockData data, @NotNull Predicate<Block> filter,
                     @NotNull Predicate<Block> physics, @NotNull Supplier<int[]> order, boolean urgent, @NotNull Runnable done) {
    submit(chunk, fromY, toY, block -> filter.test(block) ? data : null, physics, order, urgent, done);
  }

  /**
   * Same as {@link #submit(Chunk, int, int, BlockData, Predicate, Predicate, Supplier, boolean, Runnable)}, but
   * each visited block is replaced with the state {@code replacement} returns for it, or left as it is where
   * that returns null.
   */
  public void submit(@NotNull Chunk chunk, int fromY, int toY, @NotNull Function<Block, @Nullable BlockData> replacement,
                     @NotNull Predicate<Block> physics, @NotNull Supplier<int[]> order, boolean urgent, @NotNull Runnable done) {
    if (fromY > toY) return;
    Fill fill = new Fill(chunk, fromY, toY, replacement, physics, order, done);
    if (urgent) this.urgent.addFirst(fill);
    else queue.add(fill);
  }

  public void drain() {
    drain(DEFAULT_BUDGET);
  }

  /**
   * Visits up to {@code budget} blocks, spilling into the next chunk once one runs out.
   */
  public void drain(int budget) {
    drain(budget, Long.MAX_VALUE);
  }

  /**
   * Visits blocks until {@link System#nanoTime()} passes {@code deadline}, checked every
   * {@value #TIME_CHECK_INTERVAL} blocks.
   */
  public void drainUntil(long deadline) {
    drain(Integer.MAX_VALUE, deadline);
  }

  /**
   * Same as {@link #drainUntil(long)}, but once the urgent deque is empty, a background fill within
   * {@code radius} chunk rings of a player runs to completion before a farther one starts.
   */
  public void drainUntil(long deadline, @NotNull ToIntFunction<Chunk> ring, int radius) {
    if (!drainUrgent(deadline)) return;
    if (queue.isEmpty() || System.nanoTime() >= deadline) return;

    List<Fill> ordered = new ArrayList<>(queue);
    queue.clear();
    for (Fill fill : ordered) fill.ring = ring.applyAsInt(fill.chunk);
    ordered.sort((a, b) -> Integer.compare(a.ring, b.ring)); // stable: ties keep the queue order above
    queue.addAll(ordered);

    // Sorted near-first, so popping the queue from the front runs every fill within radius to completion
    // before a farther one is reached; a farther fill that has not finished its slice re-enters at the
    // back, never the front, so it never overtakes a nearer one. Every fill stays in this queue, so
    // forget() and clear() reach it regardless of ring.
    while (!queue.isEmpty() && System.nanoTime() < deadline) {
      Fill fill = queue.poll();
      if (!fill.chunk.isLoaded()) continue;
      if (fill.ring <= radius) {
        if (!runToCompletion(fill, deadline)) {
          queue.addFirst(fill);
          return;
        }
      } else if (advance(fill, TIME_CHECK_INTERVAL)) {
        fill.done.run();
      } else {
        queue.addLast(fill);
      }
    }
  }

  private void drain(int budget, long deadline) {
    while (budget > 0 && System.nanoTime() < deadline) {
      Deque<Fill> source = urgent.isEmpty() ? queue : urgent;
      Fill fill = source.peek();
      if (fill == null) return;
      if (!fill.chunk.isLoaded()) {
        source.poll();
        continue;
      }
      int before = fill.cursor;
      boolean done = advance(fill, Math.min(budget, TIME_CHECK_INTERVAL));
      budget -= fill.cursor - before;
      if (done) {
        source.poll();
        fill.done.run();
      }
    }
  }

  // Drains the urgent deque to completion, newest first, one fill at a time. False means the deadline
  // passed with fills still pending; the head fill, if any, keeps its progress for the next call.
  private boolean drainUrgent(long deadline) {
    while (!urgent.isEmpty()) {
      Fill fill = urgent.poll();
      if (!runToCompletion(fill, deadline)) {
        urgent.addFirst(fill);
        return false;
      }
    }
    return true;
  }

  // Runs a fill in TIME_CHECK_INTERVAL slices until it finishes or the deadline passes. The fill must
  // already be polled from its deque: done runs after that removal, so a done that resubmits urgently
  // never collides with the fill that is finishing. A chunk that unloads is dropped without running
  // done, same as forget().
  private boolean runToCompletion(@NotNull Fill fill, long deadline) {
    while (System.nanoTime() < deadline) {
      if (!fill.chunk.isLoaded()) return true;
      if (advance(fill, TIME_CHECK_INTERVAL)) {
        fill.done.run();
        return true;
      }
    }
    return false;
  }

  // Visits up to `blocks` more cells of the fill, continuing from where the last call left off. Returns
  // whether the fill is now finished.
  private static boolean advance(@NotNull Fill fill, int blocks) {
    if (fill.order == null) fill.order = fill.orderSupplier.get();
    int height = fill.toY - fill.fromY + 1;
    int end = Math.min(fill.order.length, fill.cursor + blocks);
    for (; fill.cursor < end; fill.cursor++) {
      int cell = fill.order[fill.cursor];
      int column = cell / height;
      Block block = fill.chunk.getBlock(column >> 4, fill.fromY + cell % height, column & 15);
      BlockData data = fill.replacement.apply(block);
      if (data != null) block.setBlockData(data, fill.physics.test(block));
    }
    return fill.cursor >= fill.order.length;
  }

  // The chunk ring distance to the nearest player in its world, for drainUntil. Player positions are read
  // once, on this call: players do not move within a tick. Built from each player's current chunk
  // coordinates rather than Chunk#getChunk(), which would load one.
  @NotNull
  public static ToIntFunction<Chunk> playerRing() {
    Map<World, int[][]> chunksByWorld = new HashMap<>();
    for (World world : Bukkit.getWorlds()) {
      List<Player> players = world.getPlayers();
      if (players.isEmpty()) continue;
      int[] xs = new int[players.size()];
      int[] zs = new int[players.size()];
      for (int i = 0; i < players.size(); i++) {
        Location location = players.get(i).getLocation();
        xs[i] = location.getBlockX() >> 4;
        zs[i] = location.getBlockZ() >> 4;
      }
      chunksByWorld.put(world, new int[][]{xs, zs});
    }

    return chunk -> {
      int[][] coords = chunksByWorld.get(chunk.getWorld());
      if (coords == null) return Integer.MAX_VALUE;
      int nearest = Integer.MAX_VALUE;
      for (int i = 0; i < coords[0].length; i++) {
        int dx = Math.abs(chunk.getX() - coords[0][i]);
        int dz = Math.abs(chunk.getZ() - coords[1][i]);
        nearest = Math.min(nearest, Math.max(dx, dz));
      }
      return nearest;
    };
  }

  /**
   * The given cells of a chunk, nearest to any player first, so a fill spreads out from where people are.
   * A cell is {@code (x * 16 + z) * height + (y - minHeight)}, the same as a fill's order.
   */
  @NotNull
  public static int[] orderByDistance(@NotNull int[] cells, @NotNull List<Location> players, int minX, int minZ, int minHeight, int height) {
    // Distance and cell are packed into one long each so that ordering is a single primitive sort.
    long[] keys = new long[cells.length];
    for (int i = 0; i < cells.length; i++) {
      int cell = cells[i];
      int column = cell / height;
      double nearest = players.isEmpty() ? 0 : Double.MAX_VALUE;
      for (Location player : players) {
        double dx = minX + (column >> 4) + 0.5 - player.getX();
        double dy = minHeight + cell % height + 0.5 - player.getY();
        double dz = minZ + (column & 15) + 0.5 - player.getZ();
        nearest = Math.min(nearest, dx * dx + dy * dy + dz * dz);
      }
      // The cell needs 20 bits (16 * 16 * 4064 at most), the rest is the distance, clamped to fit.
      keys[i] = Math.min((long) nearest, (1L << 43) - 1) << 20 | cell;
    }
    Arrays.sort(keys);

    int[] order = new int[keys.length];
    for (int i = 0; i < keys.length; i++) {
      order[i] = (int) (keys[i] & 0xFFFFF);
    }
    return order;
  }

  public void forget(@NotNull Chunk chunk) {
    urgent.removeIf(fill -> chunk.equals(fill.chunk));
    queue.removeIf(fill -> chunk.equals(fill.chunk));
  }

  public void clear() {
    urgent.clear();
    queue.clear();
  }

  @NotNull
  private static int[] shuffledColumns(int height) {
    List<Integer> columns = new ArrayList<>();
    for (int column = 0; column < COLUMNS; column++) {
      columns.add(column);
    }
    // Shuffled so a fill scatters instead of visibly sweeping from one side of the chunk to the other.
    Collections.shuffle(columns);

    int[] order = new int[COLUMNS * height];
    int next = 0;
    for (int column : columns) {
      for (int y = 0; y < height; y++) {
        order[next++] = column * height + y;
      }
    }
    return order;
  }

  private static final class Fill {

    private final Chunk chunk;
    private final int fromY;
    private final int toY;
    private final Function<Block, BlockData> replacement;
    private final Predicate<Block> physics;
    private final Supplier<int[]> orderSupplier;
    private final Runnable done;
    private int[] order;
    private int cursor;
    // Set for every queued fill when a ring-mode drain starts; a fill submitted during one counts as far.
    private int ring = Integer.MAX_VALUE;

    private Fill(Chunk chunk, int fromY, int toY, Function<Block, BlockData> replacement, Predicate<Block> physics,
                 Supplier<int[]> orderSupplier, Runnable done) {
      this.chunk = chunk;
      this.fromY = fromY;
      this.toY = toY;
      this.replacement = replacement;
      this.physics = physics;
      this.orderSupplier = orderSupplier;
      this.done = done;
    }

  }

}
