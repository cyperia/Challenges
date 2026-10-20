package net.cyperia.commons.common.collection.pair;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 */
public class Tuple<F, S> implements Pair {

  protected F first;
  protected S second;

  public Tuple() {
  }

  public Tuple(@Nullable F first, @Nullable S second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public final int amount() {
    return 2;
  }

  @NotNull
  @Override
  public final Object[] values() {
    return new Object[]{first, second};
  }

  public F getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  public void setFirst(@Nullable F first) {
    this.first = first;
  }

  public void setSecond(@Nullable S second) {
    this.second = second;
  }

  @NotNull
  @CheckReturnValue
  public <ToF, ToS> Tuple<ToF, ToS> map(@NotNull Function<? super F, ? extends ToF> firstMapper,
                                        @NotNull Function<? super S, ? extends ToS> secondMapper) {
    return of(firstMapper.apply(first), secondMapper.apply(second));
  }

  public boolean noneNull() {
    return first != null && second != null;
  }

  public boolean allNull() {
    return first == null && second == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Tuple<?, ?> tuple = (Tuple<?, ?>) o;
    return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "Tuple[" + first + ", " + second + "]";
  }

  @NotNull
  public static <F, S> Tuple<F, S> ofFirst(@Nullable F frist) {
    return new Tuple<>(frist, null);
  }

  @NotNull
  public static <F, S> Tuple<F, S> ofSecond(@Nullable S second) {
    return new Tuple<>(null, second);
  }

  @NotNull
  public static <F, S> Tuple<F, S> of(@Nullable F first, @Nullable S second) {
    return new Tuple<>(first, second);
  }

  @NotNull
  public static <F, S> Tuple<F, S> empty() {
    return new Tuple<>();
  }

}
