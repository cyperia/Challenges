package net.cyperia.commons.common.concurrent.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.Function;

public class CompletedTask<V> implements Task<V> {

  private final V value;
  private final Throwable failure;

  private CompletableFuture<V> future;

  public CompletedTask(@Nullable V value) {
    this.value = value;
    this.failure = null;
  }

  public CompletedTask(@Nullable Throwable failure) {
    this.value = null;
    this.failure = failure;
  }

  @NotNull
  @Override
  public Task<V> addListener(@NotNull TaskListener<V> listener) {
    if (failure != null) {
      listener.onFailure(this, failure);
    } else if (value != null) {
      listener.onComplete(this, value);
    } else {
      listener.onCancelled(this);
    }

    return this;
  }

  @NotNull
  @Override
  public Task<V> clearListeners() {
    return this;
  }

  @NotNull
  @Override
  public <R> Task<R> map(@Nullable Function<? super V, ? extends R> mapper) {
    if (failure != null)
      return new CompletedTask<>(failure);

    return new CompletedTask<>(value == null || mapper == null ? null : mapper.apply(value));
  }

  @Override
  public V getOrDefault(long timeout, @NotNull TimeUnit unit, V def) {
    if (value != null)
      return value;

    return def;
  }

  @Override
  public V call() throws Exception {
    return value;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    return value;
  }

  @Override
  public V get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return value;
  }

  @NotNull
  @Override
  public CompletionStage<V> stage() {
    if (future == null) {
      future = new CompletableFuture<>();

      if (failure != null) {
        future.completeExceptionally(failure);
      } else {
        future.complete(value);
      }
    }

    return future;
  }

}
