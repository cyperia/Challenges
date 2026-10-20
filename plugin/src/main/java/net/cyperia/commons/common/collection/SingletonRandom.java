package net.cyperia.commons.common.collection;

import java.util.Random;

public class SingletonRandom extends RandomWrapper {

  public static final SingletonRandom INSTANCE = new SingletonRandom();

  private SingletonRandom() {
    super(new Random());
  }

}
