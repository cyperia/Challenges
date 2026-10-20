package net.cyperia.commons.common.concurrent.task;

import net.cyperia.commons.common.collection.WrappedException;
import net.cyperia.commons.common.function.ExceptionallyFunction;
import net.cyperia.commons.common.function.ExceptionallyRunnable;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A task that may complete (done / failed / cancelled) in the future or may already be done.
 * <p>
 * For the completion can be listened by calling {@link #onComplete(Consumer)}, for the cancellation by {@link #onCancelled(Runnable)} and for failure by {@link #onFailure(Consumer)}.
 *
 * @see #asyncCall(Callable)
 * @see #completed(Object)
 */
public interface Task<V> extends Future<V>, Callable<V> {

  @NotNull
  static ExecutorService getAsyncExecutor() {
    return CompletableTask.SERVICE;
  }

  @NotNull
  static <V> Task<V> empty() {
    return completed(null);
  }

  @NotNull
  static <V> Task<V> completed(@Nullable V value) {
    return new CompletedTask<>(value);
  }

  @NotNull
  static Task<Void> completedVoid() {
    return empty();
  }

  @NotNull
  static <V> Task<V> failed(@NotNull Throwable failure) {
    return new CompletedTask<>(failure);
  }

  @NotNull
  static <V> CompletableTask<V> completable() {
    return new CompletableTask<>();
  }

  @NotNull
  static <V> Task<V> asyncCall(@NotNull Callable<V> callable) {
    return CompletableTask.callAsync(callable);
  }

  @NotNull
  static <V> Task<V> asyncSupply(@NotNull Supplier<V> supplier) {
    return asyncCall(supplier::get);
  }

  @NotNull
  static Task<Void> asyncRun(@NotNull Runnable runnable) {
    return asyncCall(() -> {
      runnable.run();
      return null;
    });
  }

  @NotNull
  static Task<Void> asyncRunExceptionally(@NotNull ExceptionallyRunnable runnable) {
    return asyncRun(runnable);
  }

  @NotNull
  static <V> Task<V> syncCall(@NotNull Callable<V> callable) {
    return CompletableTask.callSync(callable);
  }

  @NotNull
  static <V> Task<V> syncSupply(@NotNull Supplier<V> supplier) {
    return syncCall(supplier::get);
  }

  @NotNull
  static Task<Void> syncRun(@NotNull Runnable runnable) {
    return syncCall(() -> {
      runnable.run();
      return null;
    });
  }

  @NotNull
  static Task<Void> syncRunExceptionally(@NotNull ExceptionallyRunnable runnable) {
    return syncRun(runnable);
  }

  @NotNull
  default Task<V> onComplete(@NotNull Runnable action) {
    return onComplete(v -> action.run());
  }

  @NotNull
  default Task<V> onComplete(@NotNull Consumer<? super V> action) {
    return onComplete((task, value) -> action.accept(value));
  }

  @NotNull
  default Task<V> onComplete(@NotNull BiConsumer<? super Task<V>, ? super V> action) {
    return addListener(new TaskListener<V>() {
      @Override
      public void onComplete(@NotNull Task<V> task, @NotNull V value) {
        action.accept(task, value);
      }
    });
  }

  @NotNull
  default Task<V> onFailure(@NotNull Runnable action) {
    return onFailure(ex -> action.run());
  }

  @NotNull
  default Task<V> onFailure(@NotNull Consumer<? super Throwable> action) {
    return onFailure((task, ex) -> action.accept(ex));
  }

  @NotNull
  default Task<V> onFailure(@NotNull BiConsumer<? super Task<V>, ? super Throwable> action) {
    return addListener(new TaskListener<V>() {
      @Override
      public void onFailure(@NotNull Task<V> task, @NotNull Throwable ex) {
        action.accept(task, ex);
      }
    });
  }

  @NotNull
  default Task<V> throwOnFailure() {
    return onFailure(ex -> ex.printStackTrace());
  }

  @NotNull
  default Task<V> onCancelled(@NotNull Runnable action) {
    return onCancelled(task -> action.run());
  }

  @NotNull
  default Task<V> onCancelled(@NotNull Consumer<? super Task<V>> action) {
    return addListener(new TaskListener<V>() {
      @Override
      public void onCancelled(@NotNull Task<V> task) {
        action.accept(task);
      }
    });
  }

  @NotNull
  default Task<V> addListeners(@NotNull TaskListener<V>... listeners) {
    for (TaskListener<V> listener : listeners)
      addListener(listener);

    return this;
  }

  @NotNull
  Task<V> addListener(@NotNull TaskListener<V> listener);

  @NotNull
  Task<V> clearListeners();

  @Override
  V get() throws InterruptedException, ExecutionException;

  @Override
  V get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

  default V getOrDefault(V def) {
    try {
      return get();
    } catch (InterruptedException ex) {
      throw new WrappedException(ex);
    } catch (ExecutionException ex) {
      return def;
    }
  }

  default V getOrDefault(long timeout, @NotNull TimeUnit unit, V def) {
    try {
      return this.get(timeout, unit);
    } catch (InterruptedException ex) {
      throw new WrappedException(ex);
    } catch (ExecutionException | TimeoutException ex) {
      return def;
    }
  }

  default V getBeforeTimeout(long timeout, @NotNull TimeUnit unit) {
    try {
      return get(timeout, unit);
    } catch (ExecutionException | InterruptedException ex) {
      throw new WrappedException(ex);
    } catch (TimeoutException ex) {
      throw new IllegalStateException("Operation timed out (" + timeout + " " + unit + ")");
    }
  }

  @NotNull
  <R> Task<R> map(@Nullable Function<? super V, ? extends R> mapper);

  @NotNull
  default <R> Task<R> mapExceptionally(@Nullable ExceptionallyFunction<? super V, ? extends R> mapper) {
    return map(mapper);
  }

  @NotNull
  default Task<Void> mapVoid() {
    return map(v -> null);
  }

  @NotNull
  default <R> Task<R> map(@NotNull Class<R> target) {
    return map(target::cast);
  }

  @NotNull
  @CheckReturnValue
  CompletionStage<V> stage();

}
