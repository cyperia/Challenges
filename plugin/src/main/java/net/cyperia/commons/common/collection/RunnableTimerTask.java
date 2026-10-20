package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.util.TimerTask;

public class RunnableTimerTask extends TimerTask {

  protected final Runnable action;

  public RunnableTimerTask(@NotNull Runnable action) {
    this.action = action;
  }

  @Override
  public void run() {
    action.run();
  }

}
