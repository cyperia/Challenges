package net.cyperia.commons.common.collection.pair;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @see Tuple
 * @see Triple
 * @see Quadro
 */
public interface Pair {

  /**
   * @return The amount of values
   */
  int amount();

  @NotNull
  Object[] values();

  /**
   * @return {@code true} when all of the values are null, {@code false} otherwise
   */
  boolean allNull();

  /**
   * @return {@code true} when none of the values are null, {@code false} otherwise
   */
  boolean noneNull();

  @NotNull
  @Contract(value = "_, _, -> new", pure = true)
  static <F, S> Tuple<F, S> of(F first, S second) {
    return Tuple.of(first, second);
  }

  @NotNull
  @Contract(value = "_, _, _ -> new", pure = true)
  static <F, S, T> Triple<F, S, T> of(F first, S second, T third) {
    return Triple.of(first, second, third);
  }

  @NotNull
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static <F, S, T, Q> Quadro<F, S, T, Q> of(F first, S second, T third, Q fourth) {
    return Quadro.of(first, second, third, fourth);
  }

}
