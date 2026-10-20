package net.cyperia.commons.common.collection.pair;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * @param <F>  The type of the first value
 * @param <S>  The type of the second value
 * @param <T>  The type of the third value
 * @param <FF> The type of the fourth value
 */
public class Quadro<F, S, T, FF> implements Pair {

  protected F first;
  protected S second;
  protected T third;
  protected FF fourth;

  public Quadro() {
  }

  public Quadro(@Nullable F first, @Nullable S second, @Nullable T third, @Nullable FF fourth) {
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
  }

  @Override
  public final int amount() {
    return 4;
  }

  @NotNull
  @Override
  public final Object[] values() {
    return new Object[]{first, second, third, first};
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

  public FF getFourth() {
    return fourth;
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

  public void setFourth(@Nullable FF fourth) {
    this.fourth = fourth;
  }

  @NotNull
  @CheckReturnValue
  public <ToF, ToS, ToT, ToFF> Quadro<ToF, ToS, ToT, ToFF> map(@NotNull Function<? super F, ? extends ToF> firstMapper,
                                                               @NotNull Function<? super S, ? extends ToS> secondMapper,
                                                               @NotNull Function<? super T, ? extends ToT> thirdMapper,
                                                               @NotNull Function<? super FF, ? extends ToFF> fourthMapper) {
    return of(firstMapper.apply(first), secondMapper.apply(second), thirdMapper.apply(third), fourthMapper.apply(fourth));
  }

  public boolean noneNull() {
    return first != null && second != null && third != null && fourth != null;
  }

  public boolean allNull() {
    return first == null && second == null && third == null && fourth != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Quadro<?, ?, ?, ?> quadro = (Quadro<?, ?, ?, ?>) o;
    return Objects.equals(first, quadro.first) && Objects.equals(second, quadro.second) && Objects.equals(third, quadro.third) && Objects.equals(fourth, quadro.fourth);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second, third, fourth);
  }

  @Override
  public String toString() {
    return "Quadro[" + first + ", " + second + ", " + third + ", " + fourth + "]";
  }

  @NotNull
  public static <F, S, T, FF> Quadro<F, S, T, FF> ofFirst(@Nullable F first) {
    return of(first, null, null, null);
  }

  @NotNull
  public static <F, S, T, FF> Quadro<F, S, T, FF> ofSecond(@Nullable S second) {
    return of(null, second, null, null);
  }

  @NotNull
  public static <F, S, T, FF> Quadro<F, S, T, FF> ofThird(@Nullable T third) {
    return of(null, null, third, null);
  }

  @NotNull
  public static <F, S, T, FF> Quadro<F, S, T, FF> ofFourth(@Nullable FF fourth) {
    return of(null, null, null, fourth);
  }

  @NotNull
  public static <F, S, T, FF> Quadro<F, S, T, FF> of(@Nullable F first, @Nullable S second, @Nullable T third, @Nullable FF fourth) {
    return new Quadro<>(first, second, third, fourth);
  }

  @NotNull
  public static <F, S, T, FF> Quadro<F, S, T, FF> empty() {
    return new Quadro<>();
  }
}
