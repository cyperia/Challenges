package net.cyperia.commons.common.collection.pair;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 * @param <T> The type of the third value
 */
public class Triple<F, S, T> implements Pair {

  protected F first;
  protected S second;
  protected T third;

  public Triple() {
  }

  public Triple(@Nullable F first, @Nullable S second, @Nullable T third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  @Override
  public final int amount() {
    return 3;
  }

  @NotNull
  @Override
  public final Object[] values() {
    return new Object[]{first, second, third};
  }

  public F getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  public T getThird() {
    return third;
  }

  public void setFirst(@Nullable F first) {
    this.first = first;
  }

  public void setSecond(@Nullable S second) {
    this.second = second;
  }

  public void setThird(@Nullable T third) {
    this.third = third;
  }

  @NotNull
  @CheckReturnValue
  public <ToF, ToS, ToT> Triple<ToF, ToS, ToT> map(@NotNull Function<? super F, ? extends ToF> firstMapper,
                                                   @NotNull Function<? super S, ? extends ToS> secondMapper,
                                                   @NotNull Function<? super T, ? extends ToT> thirdMapper) {
    return of(firstMapper.apply(first), secondMapper.apply(second), thirdMapper.apply(third));
  }

  public boolean noneNull() {
    return first != null && second != null && third != null;
  }

  public boolean allNull() {
    return first == null && second == null && third == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(first, triple.first) && Objects.equals(second, triple.second) && Objects.equals(third, triple.third);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second, third);
  }

  @Override
  public String toString() {
    return "Triple[" + first + ", " + second + ", " + third + "]";
  }

  @NotNull
  public static <F, S, T> Triple<F, S, T> ofFirst(@Nullable F first) {
    return of(first, null, null);
  }

  @NotNull
  public static <F, S, T> Triple<F, S, T> ofSecond(@Nullable S second) {
    return of(null, second, null);
  }

  @NotNull
  public static <F, S, T> Triple<F, S, T> ofThird(@Nullable T third) {
    return of(null, null, third);
  }

  @NotNull
  public static <F, S, T> Triple<F, S, T> of(@Nullable F first, @Nullable S second, @Nullable T third) {
    return new Triple<>(first, second, third);
  }

  @NotNull
  public static <F, S, T> Triple<F, S, T> empty() {
    return new Triple<>();
  }

}
