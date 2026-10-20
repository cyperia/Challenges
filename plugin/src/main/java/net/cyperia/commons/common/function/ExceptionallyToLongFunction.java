package net.cyperia.commons.common.function;

import net.cyperia.commons.common.collection.WrappedException;

import java.util.function.ToLongFunction;

@FunctionalInterface
public interface ExceptionallyToLongFunction<T> extends ToLongFunction<T> {

  @Override
  default long applyAsLong(T t) {
    try {
      return applyExceptionally(t);
    } catch (Exception ex) {
      throw WrappedException.rethrow(ex);
    }
  }

  long applyExceptionally(T t) throws Exception;

}
