package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.Nullable;

/**
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 */
@Deprecated
public class Tuple<F, S> extends net.cyperia.commons.common.collection.pair.Tuple<F, S> {

  public Tuple() {
  }

  public Tuple(@Nullable F first, @Nullable S second) {
    super(first, second);
  }

}
