package net.cyperia.commons.common.function;

import net.cyperia.commons.common.collection.WrappedException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@FunctionalInterface
public interface ExceptionallyFunction<T, R> extends Function<T, R> {

  @Override
  default R apply(T t) {
    try {
      return applyExceptionally(t);
    } catch (Exception ex) {
      throw WrappedException.rethrow(ex);
    }
  }

  R applyExceptionally(T t) throws Exception;

  @NotNull
  static <T> ExceptionallyFunction<T, T> identity() {
    return t -> t;
  }

}
