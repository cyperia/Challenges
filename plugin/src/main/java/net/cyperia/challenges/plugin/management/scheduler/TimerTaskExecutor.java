package net.cyperia.challenges.plugin.management.scheduler;

import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

final class TimerTaskExecutor extends AbstractTaskExecutor {

  private final Plugin plugin;
  private final TimerTaskConfig config;

  TimerTaskExecutor(@NotNull Plugin plugin, @NotNull TimerTaskConfig config) {
    this.plugin = plugin;
    this.config = config;
  }

  // Sync handlers run on the global region, inline when the caller already is on it
  public void execute() {
    if (config.isAsync()) Regions.async(plugin, this);
    else Regions.global(plugin, this);
  }

  @NotNull
  @Override
  public TimerTaskConfig getConfig() {
    return config;
  }

}
