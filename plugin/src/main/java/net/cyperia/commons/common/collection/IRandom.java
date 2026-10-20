package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public interface IRandom {

  @NotNull
  @CheckReturnValue
  static IRandom create() {
    return new SeededRandomWrapper();
  }

  @NotNull
  @CheckReturnValue
  static IRandom create(long seed) {
    return new SeededRandomWrapper(seed);
  }

  @NotNull
  @CheckReturnValue
  static IRandom wrap(@NotNull Random random) {
    return new RandomWrapper(random);
  }

  @NotNull
  @CheckReturnValue
  static IRandom threadLocal() {
    return wrap(ThreadLocalRandom.current());
  }

  @NotNull
  @CheckReturnValue
  static IRandom secure() {
    return wrap(new SecureRandom());
  }

  @NotNull
  @CheckReturnValue
  static IRandom singleton() {
    return SingletonRandom.INSTANCE;
  }

  long getSeed();

  void setSeed(long seed);

  void nextBytes(@NotNull byte[] bytes);

  boolean nextBoolean();

  int nextInt();

  int nextInt(int bound);

  @NotNull
  IntStream ints();

  @NotNull
  IntStream ints(long streamSize);

  @NotNull
  IntStream ints(int randomNumberOrigin, int randomNumberBound);

  @NotNull
  IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound);

  @NotNull
  LongStream longs();

  long nextLong();

  @NotNull
  LongStream longs(long streamSize);

  @NotNull
  LongStream longs(long randomNumberOrigin, long randomNumberBound);

  @NotNull
  LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound);

  double nextDouble();

  double nextGaussian();

  @NotNull
  DoubleStream doubles();

  @NotNull
  DoubleStream doubles(long streamSize);

  @NotNull
  DoubleStream doubles(double randomNumberOrigin, double randomNumberBound);

  @NotNull
  DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound);

  float nextFloat();

  default <T> T choose(@NotNull T... array) {
    if (array.length == 0) throw new IllegalArgumentException("Array is empty");
    return array[nextInt(array.length)];
  }

  default <T> T choose(@NotNull List<? extends T> list) {
    if (list.isEmpty()) throw new IllegalArgumentException("List is empty");
    return list.get(nextInt(list.size()));
  }

  default <T> T choose(@NotNull Collection<? extends T> collection) {
    if (collection.isEmpty()) throw new IllegalArgumentException("Collection is empty");
    return choose(new ArrayList<>(collection));
  }

  default void shuffle(@NotNull List<?> list) {
    Collections.shuffle(list, asRandom());
  }

  default int around(int value, int range) {
    return range(value - range, value + range);
  }

  default int range(int min, int max) {
    if (min >= max) throw new IllegalArgumentException("min >= max");
    return nextInt(max - min) + min;
  }

  @NotNull
  @CheckReturnValue
  default Random asRandom() {
    if (!(this instanceof Random))
      throw new IllegalStateException(this.getClass().getName() + " cannot be converted a java.util.Random");
    return (Random) this;
  }

}
