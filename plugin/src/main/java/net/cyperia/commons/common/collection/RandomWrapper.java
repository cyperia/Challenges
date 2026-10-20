package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class RandomWrapper implements IRandom {

  private final Random random;

  public RandomWrapper(@NotNull Random random) {
    this.random = random;
  }

  @Override
  public long getSeed() {
    throw new UnsupportedOperationException("Random.getSeed()");
  }

  @Override
  public void setSeed(long seed) {
    random.setSeed(seed);
  }

  @Override
  public void nextBytes(@NotNull byte[] bytes) {
    random.nextBytes(bytes);
  }

  @Override
  public boolean nextBoolean() {
    return random.nextBoolean();
  }

  @Override
  public int nextInt() {
    return random.nextInt();
  }

  @Override
  public int nextInt(int bound) {
    return random.nextInt(bound);
  }

  @NotNull
  @Override
  public IntStream ints() {
    return random.ints();
  }

  @NotNull
  @Override
  public IntStream ints(long streamSize) {
    return random.ints(streamSize);
  }

  @NotNull
  @Override
  public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
    return random.ints(randomNumberOrigin, randomNumberBound);
  }

  @NotNull
  @Override
  public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
    return random.ints(streamSize, randomNumberOrigin, randomNumberBound);
  }

  @NotNull
  @Override
  public LongStream longs() {
    return random.longs();
  }

  @Override
  public long nextLong() {
    return random.nextLong();
  }

  @NotNull
  @Override
  public LongStream longs(long streamSize) {
    return random.longs(streamSize);
  }

  @NotNull
  @Override
  public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
    return random.longs(randomNumberOrigin, randomNumberBound);
  }

  @NotNull
  @Override
  public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
    return random.longs(streamSize, randomNumberOrigin, randomNumberBound);
  }

  @Override
  public double nextDouble() {
    return random.nextDouble();
  }

  @Override
  public double nextGaussian() {
    return random.nextGaussian();
  }

  @NotNull
  @Override
  public DoubleStream doubles() {
    return random.doubles();
  }

  @NotNull
  @Override
  public DoubleStream doubles(long streamSize) {
    return random.doubles(streamSize);
  }

  @NotNull
  @Override
  public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
    return random.doubles(randomNumberOrigin, randomNumberBound);
  }

  @NotNull
  @Override
  public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
    return random.doubles(streamSize, randomNumberOrigin, randomNumberBound);
  }

  @Override
  public float nextFloat() {
    return random.nextFloat();
  }

  @NotNull
  @Override
  public Random asRandom() {
    return random;
  }

  @Override
  public String toString() {
    return "Random[wrapped=" + random.getClass().getSimpleName() + "]";
  }
}
