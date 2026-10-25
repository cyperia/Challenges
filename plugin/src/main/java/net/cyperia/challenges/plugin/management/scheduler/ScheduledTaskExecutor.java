package net.cyperia.challenges.plugin.management.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

// A sync executor ticks on the global region and an async one on the async scheduler. Handlers that
// work on players or blocks hop to the owning thread themselves.
final class ScheduledTaskExecutor extends AbstractTaskExecutor {

  private final Plugin plugin;
  private final ScheduledTaskConfig config;
  private volatile ScheduledTask task;

  ScheduledTaskExecutor(@NotNull Plugin plugin, @NotNull ScheduledTaskConfig config) {
    this.plugin = plugin;
    this.config = config;
  }

  public void stop() {
    ScheduledTask running = task;
    if (running != null && !running.isCancelled()) {
      running.cancel();
      task = null;
    }
  }

  public void start() {
    task = config.isAsync()
      ? Regions.asyncTimer(plugin, _ -> run(), 1, config.getRate())
      : Regions.timer(plugin, _ -> run(), 1, config.getRate());
  }

  @NotNull
  @Override
  public ScheduledTaskConfig getConfig() {
    return config;
  }

}
