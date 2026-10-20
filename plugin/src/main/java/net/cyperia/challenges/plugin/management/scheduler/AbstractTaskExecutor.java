package net.cyperia.challenges.plugin.management.scheduler;

import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTaskExecutor implements Runnable {

  protected final List<ScheduledFunction> functions = new ArrayList<>(1);

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
