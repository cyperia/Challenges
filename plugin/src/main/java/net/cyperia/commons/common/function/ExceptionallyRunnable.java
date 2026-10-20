package net.cyperia.commons.common.function;

import net.cyperia.commons.common.collection.WrappedException;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface ExceptionallyRunnable extends Runnable, Callable<Void> {

  @Override
  default void run() {
    try {
      runExceptionally();
    } catch (Exception ex) {
      throw WrappedException.rethrow(ex);
    }
  }

  @Override
  default Void call() throws Exception {
    runExceptionally();
    return null;
  }

  void runExceptionally() throws Exception;

}
