package net.cyperia.commons.common.concurrent.task;

import net.cyperia.commons.common.collection.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.function.Function;

public class CompletableTask<V> implements Task<V> {

  static final ExecutorService SERVICE = Executors.newCachedThreadPool(new NamedThreadFactory("TaskProcessor"));

  private final Collection<TaskListener<V>> listeners = new ArrayList<>();
  private final CompletableFuture<V> future;

  private Throwable failure;

  public CompletableTask() {
    this(new CompletableFuture<>());
  }

  private CompletableTask(@NotNull CompletableFuture<V> future) {
    this.future = future;
    this.future.exceptionally(ex -> {
      this.failure = ex;
      return null;
    });
  }

  @NotNull
  public static <V> Task<V> callAsync(@NotNull Callable<V> callable) {
    CompletableTask<V> task = new CompletableTask<>();
    SERVICE.execute(() -> {
      try {
        task.complete(callable.call());
      } catch (Throwable ex) {
        task.fail(ex);
      }
    });
    return task;
  }

  @NotNull
  public static <V> Task<V> callSync(@NotNull Callable<V> callable) {
    CompletableTask<V> task = new CompletableTask<>();
    try {
      task.complete(callable.call());
    } catch (Throwable ex) {
      task.fail(ex);
    }
    return task;
  }

  @NotNull
  @Override
  public Task<V> addListener(@NotNull TaskListener<V> listener) {
    if (future.isDone()) {
      V value = future.getNow(null);
      if (future.isCancelled() || value != null) {
        listener.onCancelled(this);
      } else if (failure != null) {
        listener.onFailure(this, failure);
      } else {
        listener.onComplete(this, value);
      }
      return this;
    }

    listeners.add(listener);
    return this;
  }

  @NotNull
  @Override
  public Task<V> clearListeners() {
    this.listeners.clear();
    return this;
  }

  public void fail(Throwable throwable) {
    this.failure = throwable;
    this.future.completeExceptionally(throwable);
    for (TaskListener<V> listener : this.listeners) {
      listener.onFailure(this, throwable);
    }
  }

  @Override
  public V call() {
    if (this.future.isDone()) {
      return this.future.getNow(null);
    }
    throw new UnsupportedOperationException("Use #complete in the CompletableTask");
  }

  public void complete(@Nullable V value) {
    future.complete(value);
    if (value != null) {
      for (TaskListener<V> listener : listeners) {
        listener.onComplete(this, value);
      }
    } else {
      for (TaskListener<V> listener : listeners) {
        listener.onCancelled(this);
      }
    }

  }

  @Override
  public boolean cancel(boolean b) {
    if (this.future.isCancelled()) {
      return false;
    }

    if (this.future.cancel(b)) {
      for (TaskListener<V> listener : this.listeners) {
        listener.onCancelled(this);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean isCancelled() {
    return this.future.isCancelled();
  }

  @Override
  public boolean isDone() {
    return this.future.isDone();
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    return future.get();
  }

  @Override
  public V get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(timeout, unit);
  }

  @NotNull
  @Override
  public <R> Task<R> map(@Nullable Function<? super V, ? extends R> mapper) {
    CompletableTask<R> task = new CompletableTask<>();
    this.future.thenAccept(v -> {
      try {
        task.complete(mapper == null ? null : mapper.apply(v));
      } catch (Throwable ex) {
        task.fail(ex);
      }
    });
    this.onFailure(task.future::completeExceptionally);
    this.onCancelled(otherTask -> task.cancel(true));
    return task;
  }

  @NotNull
  @Override
  public CompletionStage<V> stage() {
    return future;
  }
}
