package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

public class NamedThreadFactory implements ThreadFactory {

  private static final AtomicInteger poolNumber = new AtomicInteger(1);

  protected final int id = poolNumber.getAndIncrement();
  protected final IntFunction<String> nameFunction;
  protected final ThreadGroup group;
  protected final AtomicInteger threadNumber = new AtomicInteger(1);

  public NamedThreadFactory(@NotNull IntFunction<String> nameFunction) {
    this.group = Thread.currentThread().getThreadGroup();
    this.nameFunction = nameFunction;
  }

  public NamedThreadFactory(@NotNull String prefix) {
    this(id -> prefix + "-" + id);
  }

  @Override
  public Thread newThread(@NotNull Runnable task) {
    Thread thread = new Thread(group, task, nameFunction.apply(threadNumber.getAndIncrement()));
    if (thread.isDaemon())
      thread.setDaemon(false);
    if (thread.getPriority() != Thread.NORM_PRIORITY)
      thread.setPriority(Thread.NORM_PRIORITY);
    return thread;
  }

}
