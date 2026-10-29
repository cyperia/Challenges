package net.cyperia.commons.bukkit.utils.misc;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Runs code on the thread that owns what it touches. On Paper and Purpur every check is true on the
 * main thread, so the immediate variants run inline exactly as the code did before regionised servers.
 * A retired entity runs neither the task nor its scheduler's retired callback when the schedule call
 * reports it, so the retired fallback is run here. Once the plugin is disabled nothing can be scheduled:
 * a task then only runs when the calling thread owns its target, otherwise it is dropped.
 */
public final class Regions {

  // Only Folia based servers have a regionised server
  private static final boolean REGIONIZED = isClassPresent("io.papermc.paper.threadedregions.RegionizedServer");

  // Tests replace this to simulate a regionised server.
  static BooleanSupplier regionized = () -> REGIONIZED;

  private Regions() {
  }

  private static boolean isClassPresent(@NotNull String name) {
    try {
      Class.forName(name, false, Regions.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  public static void global(@NotNull Plugin plugin, @NotNull Runnable task) {
    // A regionised server halts the global scheduler before it disables plugins on its shutdown thread
    if (Bukkit.isGlobalTickThread() || (Bukkit.isPrimaryThread() && Bukkit.isStopping())) task.run();
    else if (plugin.isEnabled()) Bukkit.getGlobalRegionScheduler().execute(plugin, task);
  }

  public static void entity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task) {
    entity(plugin, entity, task, null);
  }

  public static void entity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable retired) {
    if (Bukkit.isOwnedByCurrentRegion(entity)) task.run();
    else if (!plugin.isEnabled() || !entity.getScheduler().execute(plugin, task, retired, 1)) {
      if (retired != null) retired.run();
    }
  }

  /**
   * Removes the entity on its owning thread. While a regionised server stops, region data is gone
   * and {@code remove} throws, so the entity is only marked non-persistent and is not saved.
   */
  public static void remove(@NotNull Plugin plugin, @NotNull Entity entity) {
    if (regionized.getAsBoolean() && Bukkit.isStopping()) {
      entity.setPersistent(false);
      return;
    }
    entity(plugin, entity, () -> {
      if (entity.isValid()) entity.remove();
    });
  }

  public static void at(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable task) {
    if (Bukkit.isOwnedByCurrentRegion(location)) task.run();
    else if (plugin.isEnabled()) Bukkit.getRegionScheduler().execute(plugin, location, task);
  }

  /**
   * Runs {@code scan} once in every region that has a player in {@code world}, for scans over
   * {@code World#getEntities()} and the like, which only see the entities of the calling region.
   * The future completes on the global region once every region is done. Regions without a player are
   * not visited. On Paper and Purpur this is one scan of the whole world.
   */
  @NotNull
  public static CompletableFuture<Void> forEachRegion(@NotNull Plugin plugin, @NotNull World world, @NotNull Runnable scan) {
    CompletableFuture<Void> done = new CompletableFuture<>();
    global(plugin, () -> {
      if (!plugin.isEnabled()) {
        runScan(plugin, world, scan);
        done.complete(null);
        return;
      }

      List<Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld() == world).collect(Collectors.toList());
      if (players.isEmpty()) {
        if (!regionized.getAsBoolean()) runScan(plugin, world, scan);
        done.complete(null);
        return;
      }

      List<Location> claimed = new CopyOnWriteArrayList<>();
      AtomicInteger remaining = new AtomicInteger(players.size());
      Runnable finished = () -> {
        if (remaining.decrementAndGet() == 0) global(plugin, () -> done.complete(null));
      };
      for (Player player : players) {
        entity(plugin, player, () -> {
          try {
            if (claim(claimed, player)) runScan(plugin, world, scan);
          } finally {
            finished.run();
          }
        }, finished);
      }
    });
    return done;
  }

  // Players standing in one region run one after another, so the first claims the region for the others
  private static boolean claim(@NotNull List<Location> claimed, @NotNull Player player) {
    for (Location other : claimed) {
      if (Bukkit.isOwnedByCurrentRegion(other)) return false;
    }
    claimed.add(player.getLocation());
    return true;
  }

  private static void runScan(@NotNull Plugin plugin, @NotNull World world, @NotNull Runnable scan) {
    try {
      scan.run();
    } catch (RuntimeException ex) {
      plugin.getLogger().log(Level.SEVERE, "A scan of world " + world.getName() + " failed", ex);
    }
  }

  public static boolean isGlobalThread() {
    return Bukkit.isGlobalTickThread();
  }

  @NotNull
  public static ScheduledTask nextTick(@NotNull Plugin plugin, @NotNull Runnable task) {
    return Bukkit.getGlobalRegionScheduler().run(plugin, _ -> task.run());
  }

  @NotNull
  public static ScheduledTask later(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
    if (delayTicks < 1) return nextTick(plugin, task);
    return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, _ -> task.run(), delayTicks);
  }

  @NotNull
  public static ScheduledTask timer(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
    return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task, Math.max(delayTicks, 1), periodTicks);
  }

  // Null when the entity is gone before the task could be queued
  @Nullable
  public static ScheduledTask nextTick(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task) {
    return entity.getScheduler().run(plugin, _ -> task.run(), null);
  }

  @Nullable
  public static ScheduledTask later(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task, long delayTicks) {
    if (delayTicks < 1) return nextTick(plugin, entity, task);
    return entity.getScheduler().runDelayed(plugin, _ -> task.run(), null, delayTicks);
  }

  @Nullable
  public static ScheduledTask timer(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
    return entity.getScheduler().runAtFixedRate(plugin, task, null, Math.max(delayTicks, 1), periodTicks);
  }

  @NotNull
  public static ScheduledTask nextTick(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable task) {
    return Bukkit.getRegionScheduler().run(plugin, location, _ -> task.run());
  }

  @NotNull
  public static ScheduledTask later(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable task, long delayTicks) {
    if (delayTicks < 1) return nextTick(plugin, location, task);
    return Bukkit.getRegionScheduler().runDelayed(plugin, location, _ -> task.run(), delayTicks);
  }

  @NotNull
  public static ScheduledTask timer(@NotNull Plugin plugin, @NotNull Location location, @NotNull Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
    return Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, task, Math.max(delayTicks, 1), periodTicks);
  }

  public static void async(@NotNull Plugin plugin, @NotNull Runnable task) {
    Bukkit.getAsyncScheduler().runNow(plugin, _ -> task.run());
  }

  @NotNull
  public static ScheduledTask asyncLater(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
    if (delayTicks < 1) {
      return Bukkit.getAsyncScheduler().runNow(plugin, _ -> task.run());
    }
    return Bukkit.getAsyncScheduler().runDelayed(plugin, _ -> task.run(), delayTicks * 50, TimeUnit.MILLISECONDS);
  }

  @NotNull
  public static ScheduledTask asyncTimer(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
    return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task, Math.max(delayTicks, 1) * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
  }

}
