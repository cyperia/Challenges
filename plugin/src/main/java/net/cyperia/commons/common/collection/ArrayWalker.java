package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class ArrayWalker<T> implements Iterable<T> {

  protected final Object array;
  protected final int length;

  protected ArrayWalker(@NotNull Object array) {
    if (!array.getClass().isArray())
      throw new IllegalArgumentException(array.getClass().getName() + " is not an array");
    this.array = array;
    this.length = Array.getLength(array);
  }

  public static <T> ArrayWalker<T> walk(@NotNull Object array) {
    return new ArrayWalker<>(array);
  }

  public static <T> ArrayWalker<T> walk(@NotNull T... array) {
    return new ArrayWalker<>(array);
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      private int cursor = 0;

      @Override
      public boolean hasNext() {
        return cursor < length;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        return (T) Array.get(array, cursor++);
      }

    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public void forEach(Consumer<? super T> action) {
    for (int i = 0; i < length; i++) {
      action.accept((T) Array.get(array, i));
    }
  }
}
