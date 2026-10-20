package net.cyperia.challenges.plugin.management.scheduler;

import net.cyperia.challenges.plugin.Challenges;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

final class TimerTaskExecutor extends AbstractTaskExecutor {

  private final TimerTaskConfig config;

  TimerTaskExecutor(@NotNull TimerTaskConfig config) {
    this.config = config;
  }

  public void execute() {
    if (config.isAsync())
      Bukkit.getScheduler().runTaskAsynchronously(Challenges.getInstance(), this);
    else if (!Bukkit.isPrimaryThread())
      Bukkit.getScheduler().runTask(Challenges.getInstance(), this);
    else this.run();
  }

  @NotNull
  @Override
  public TimerTaskConfig getConfig() {
    return config;
  }

}
