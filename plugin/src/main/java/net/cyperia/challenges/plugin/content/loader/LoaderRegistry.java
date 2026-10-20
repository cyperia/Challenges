package net.cyperia.challenges.plugin.content.loader;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.utils.logging.ConsolePrint;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoaderRegistry {

  private static final AtomicInteger loading = new AtomicInteger(); // Keeps track of how many loaders are still loading
  private final Map<Class<? extends ContentLoader>, Subscribers> subscribers = new HashMap<>();
  private final Collection<ContentLoader> loaders;

  public LoaderRegistry(@NotNull ContentLoader... loaders) {
    this.loaders = Arrays.asList(loaders);
  }

  private static void execute(@NotNull Class<? extends ContentLoader> classOfLoader, @NotNull Runnable action) {
    try {
      action.run();
    } catch (Exception ex) {
      Logger.error("Could not execute subscriber for {}", classOfLoader.getSimpleName(), ex);
    }
  }

  public void load() {
    if (isLoading()) {
      ConsolePrint.alreadyExecutingContentLoader();
      return;
    }

    loaders.forEach(this::executeLoader);
  }

  private void executeLoader(@NotNull ContentLoader loader) {
    loading.incrementAndGet();
    loader.load();
    loading.decrementAndGet();
    handleCompleteLoading(loader.getClass());
  }

  private void handleCompleteLoading(@NotNull Class<? extends ContentLoader> classOfLoader) {
    Logger.debug("{} finished loading. {} loader(s) left", classOfLoader.getSimpleName(), loading);

    if (loading.get() == 0)
      Logger.info("All loaders finished loading");

    if (Challenges.getInstance().isEnabled()) {
      Subscribers subscribers = this.subscribers.get(classOfLoader);
      if (subscribers == null) return;
      subscribers.execute();
    }
  }


  public void recallSubscribers(@NotNull Class<? extends ContentLoader> classOfLoader) {
    Subscribers subscribers = this.subscribers.get(classOfLoader);
    if (subscribers != null && subscribers.executed) {
      callSubscribers(classOfLoader);
    }
  }

  void callSubscribers(@NotNull Class<? extends ContentLoader> classOfLoader) {
    Logger.debug("{} is executing it's subscribers..", classOfLoader.getSimpleName());
    Subscribers subscribers = this.subscribers.get(classOfLoader);
    if (subscribers == null) return;
    if (Challenges.getInstance().isEnabled()) {
      ChallengeHelper.runSync(subscribers::execute);
    } else {
      subscribers.markForExecution();
    }
  }

  public void enable() {
    for (Subscribers subscribers : subscribers.values()) {
      if (!subscribers.markedForExecution || subscribers.executed) continue;
      subscribers.execute();
    }
  }

  public void disable() {
    subscribers.clear();
  }

  public boolean isLoading() {
    return loading.get() > 0;
  }

  public void subscribe(@NotNull Class<? extends ContentLoader> classOfLoader, @NotNull Runnable action) {
    Subscribers subscribers = this.subscribers.computeIfAbsent(classOfLoader, key -> new Subscribers(classOfLoader));
    subscribers.actions.add(action);

    if (subscribers.executed)
      execute(classOfLoader, action);
  }

  @NotNull
  public <T extends ContentLoader> Optional<T> getFirstLoaderByClass(@NotNull Class<T> clazz) {
    for (ContentLoader loader : loaders) {
      if (clazz.isAssignableFrom(loader.getClass())) {
        return Optional.of(clazz.cast(loader));
      }
    }
    return Optional.empty();
  }

  @NotNull
  public <T extends ContentLoader> T findLoaderByClassOrThrow(@NotNull Class<T> clazz) {
    return getFirstLoaderByClass(clazz).orElseThrow(() -> new NoSuchElementException("No loader of type " + clazz.getSimpleName() + " found"));
  }

  private static class Subscribers {

    private final Class<? extends ContentLoader> loader;
    private final Collection<Runnable> actions = new ArrayList<>(1);
    private boolean executed = false;
    private boolean markedForExecution = false;

    public Subscribers(Class<? extends ContentLoader> loader) {
      this.loader = loader;
    }

    public void markForExecution() {
      markedForExecution = true;
    }

    public void execute() {
      Logger.debug("Executing subscribers for {}", loader.getSimpleName());
      executed = true;
      for (Runnable subscriber : actions) {
        LoaderRegistry.execute(loader, subscriber);
      }
    }

  }
}
