package net.cyperia.commons.bukkit.utils.wrapper;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public final class ActionListener<E extends Event> implements Listener {

  private final Class<E> classOfEvent;
  private final Consumer<? super E> listener;
  private final EventPriority priority;
  private final boolean ignoreCancelled;

  public ActionListener(@NotNull Class<E> classOfEvent, @NotNull Consumer<? super E> listener, @NotNull EventPriority priority, boolean ignoreCancelled) {
    this.classOfEvent = classOfEvent;
    this.listener = listener;
    this.priority = priority;
    this.ignoreCancelled = ignoreCancelled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActionListener<?> that = (ActionListener<?>) o;
    return listener.equals(that.listener);
  }

  @Override
  public int hashCode() {
    return Objects.hash(listener);
  }

  @NotNull
  public Consumer<? super E> getListener() {
    return listener;
  }

  @NotNull
  public EventPriority getPriority() {
    return priority;
  }

  @NotNull
  public Class<E> getClassOfEvent() {
    return classOfEvent;
  }

  public boolean isIgnoreCancelled() {
    return ignoreCancelled;
  }

}
