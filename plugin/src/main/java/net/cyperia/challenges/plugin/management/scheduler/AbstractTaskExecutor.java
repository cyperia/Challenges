package net.cyperia.challenges.plugin.management.scheduler;

import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractTaskExecutor implements Runnable {

  // registered from any thread while the executor's own thread iterates
  protected final List<ScheduledFunction> functions = new CopyOnWriteArrayList<>();

  @Override
  public void run() {
    for (ScheduledFunction function : functions) {
      try {
        function.invoke();
      } catch (InvocationTargetException | IllegalAccessException ex) {
        Logger.error("An exception occurred while executing {}", function, ex);
      }
    }
  }

  @NotNull
  public abstract AbstractTaskConfig getConfig();

  public void register(@NotNull ScheduledFunction function) {
    functions.add(function);
  }

  public void unregister(@NotNull Object holder) {
    functions.removeIf(function -> function.getHolder() == holder);
  }

}
