package net.cyperia.challenges.plugin.utils.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ListBuilder<T> {

  private final List<T> list = new ArrayList<>();

  @SafeVarargs
  public ListBuilder(T... t) {
    list.addAll(Arrays.asList(t));
  }

  public ListBuilder<T> fill(Consumer<ListBuilder<T>> consumer) {
    consumer.accept(this);
    return this;
  }

  @SafeVarargs
  public final ListBuilder<T> addAll(T... t) {
    return addAll(Arrays.asList(t));
  }

  public ListBuilder<T> addAll(Collection<T> collection) {
    list.addAll(collection);
    return this;
  }

  @SafeVarargs
  public final ListBuilder<T> addAllIfNotContains(T... t) {
    return addAllIfNotContains(Arrays.asList(t));
  }

  public ListBuilder<T> addAllIfNotContains(Collection<T> collection) {
    for (T t : collection) {
      if (!list.contains(t)) list.add(t);
    }
    return this;
  }

  public ListBuilder<T> add(T t) {
    list.add(t);
    return this;
  }

  public ListBuilder<T> addIfNotContains(T t) {
    if (list.contains(t)) return this;
    return add(t);
  }

  public ListBuilder<T> remove(T t) {
    list.remove(t);
    return this;
  }

  public ListBuilder<T> forEach(Consumer<? super T> action) {
    list.forEach(action);
    return this;
  }

  public ListBuilder<T> removeIf(Predicate<? super T> action) {
    list.removeIf(action);
    return this;
  }

  public List<T> build() {
    return list;
  }

}
