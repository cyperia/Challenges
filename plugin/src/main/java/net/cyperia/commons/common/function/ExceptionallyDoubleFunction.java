package net.cyperia.commons.common.function;

import net.cyperia.commons.common.collection.WrappedException;

import java.util.function.DoubleFunction;

@FunctionalInterface
public interface ExceptionallyDoubleFunction<R> extends DoubleFunction<R> {

  @Override
  default R apply(double value) {
    try {
      return applyExceptionally(value);
    } catch (Exception ex) {
      throw WrappedException.rethrow(ex);
    }
  }

  R applyExceptionally(double value) throws Exception;

}
