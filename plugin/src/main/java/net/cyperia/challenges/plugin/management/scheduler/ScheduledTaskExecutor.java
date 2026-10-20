package net.cyperia.challenges.plugin.management.scheduler;

import net.cyperia.challenges.plugin.Challenges;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

final class ScheduledTaskExecutor extends AbstractTaskExecutor {

  private final ScheduledTaskConfig config;
  private BukkitTask task;

  ScheduledTaskExecutor(@NotNull ScheduledTaskConfig config) {
    this.config = config;
  }

  public void stop() {
    if (task != null && !task.isCancelled()) {
      task.cancel();
      task = null;
    }
  }

  public void start() {
    BukkitScheduler scheduler = Bukkit.getScheduler();
    Challenges plugin = Challenges.getInstance();
    task = config.isAsync() ? scheduler.runTaskTimerAsynchronously(plugin, this, 0, config.getRate()) :
      scheduler.runTaskTimer(plugin, this, 0, config.getRate());
  }

  @NotNull
  @Override
  public ScheduledTaskConfig getConfig() {
    return config;
  }

}
