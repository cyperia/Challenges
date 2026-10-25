package net.cyperia.challenges.plugin.management.scheduler;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.misc.ReflectionUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ScheduleManager {

  private final Plugin plugin;
  private final Map<ScheduledTaskConfig, ScheduledTaskExecutor> scheduledTaskExecutorsByConfig = new ConcurrentHashMap<>();
  private final Map<TimerTaskConfig, TimerTaskExecutor> timerTaskExecutorsByConfig = new ConcurrentHashMap<>();
  private volatile boolean started = false;

  public ScheduleManager(@NotNull Plugin plugin) {
    this.plugin = plugin;
  }

  public void register(@NotNull Object... schedulers) {
    for (Object scheduler : schedulers) {
      register(scheduler);
    }
  }

  public void register(@NotNull Object scheduler) {
    for (Method method : ReflectionUtils.getMethodsAnnotatedWith(scheduler.getClass(), ScheduledTask.class)) {
      if (method.getParameterCount() != 0) {
        Logger.warn("Could not register scheduler " + method);
        continue;
      }

      ScheduledTask annotation = method.getAnnotation(ScheduledTask.class);
      ScheduledFunction function = new ScheduledFunction(scheduler, method, new PoliciesContainer(annotation));

      Logger.debug("Registered scheduled task {}", function);
      register(function, new ScheduledTaskConfig(annotation));
    }
    for (Method method : ReflectionUtils.getMethodsAnnotatedWith(scheduler.getClass(), TimerTask.class)) {
      if (method.getParameterCount() != 0) {
        Logger.warn("Could not register scheduler {}", method);
        continue;
      }

      TimerTask annotation = method.getAnnotation(TimerTask.class);
      ScheduledFunction function = new ScheduledFunction(scheduler, method, new PoliciesContainer(annotation));

      Logger.debug("Registered timer task {}", function);
      register(function, new TimerTaskConfig(annotation));
    }
  }

  public void unregister(@NotNull Object object) {
    for (ScheduledTaskExecutor scheduler : scheduledTaskExecutorsByConfig.values()) {
      scheduler.unregister(object);
    }
  }

  private void register(@NotNull ScheduledFunction function, @NotNull AbstractTaskConfig config) {
    if (config instanceof ScheduledTaskConfig taskConfig) {
      if (taskConfig.getRate() < 1) {
        Logger.warn("Schedule rate cannot be less than 1; Could not register {}", function);
        return;
      }

      ScheduledTaskExecutor executor = getOrCreateScheduledTaskExecutor(taskConfig);
      executor.register(function);
    }
    if (config instanceof TimerTaskConfig taskConfig) {
      TimerTaskExecutor executor = getOrCreateTimerTaskExecutor(taskConfig);
      executor.register(function);
    }
  }

  @NotNull
  private ScheduledTaskExecutor getOrCreateScheduledTaskExecutor(@NotNull ScheduledTaskConfig config) {
    ScheduledTaskExecutor executor = scheduledTaskExecutorsByConfig.get(config);
    if (executor != null) return executor;

    // Create a new task
    executor = new ScheduledTaskExecutor(plugin, config);
    if (started) executor.start();
    scheduledTaskExecutorsByConfig.put(config, executor);
    return executor;
  }

  @NotNull
  private TimerTaskExecutor getOrCreateTimerTaskExecutor(@NotNull TimerTaskConfig config) {
    TimerTaskExecutor executor = timerTaskExecutorsByConfig.get(config);
    if (executor != null) return executor;

    // Create a new task
    executor = new TimerTaskExecutor(plugin, config);
    timerTaskExecutorsByConfig.put(config, executor);
    return executor;
  }

  public void fireTimerStatusChange() {
    if (!started) return;
    for (TimerTaskExecutor executor : timerTaskExecutorsByConfig.values()) {
      if (!executor.getConfig().acceptsStatus(ChallengeAPI.getTimerStatus())) continue;
      executor.execute();
    }
  }

  public void stop() {
    started = false;
    scheduledTaskExecutorsByConfig.values().forEach(ScheduledTaskExecutor::stop);
    scheduledTaskExecutorsByConfig.clear();
    timerTaskExecutorsByConfig.clear();
  }

  public void start() {
    started = true;
    scheduledTaskExecutorsByConfig.values().forEach(ScheduledTaskExecutor::start);
  }

}
