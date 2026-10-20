package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is used to rethrow signed exception as unsigned exceptions.
 */
public class WrappedException extends RuntimeException {

  public static class SilentWrappedException extends WrappedException {

    public SilentWrappedException(@Nullable String message, @NotNull Throwable cause) {
      super(message, cause);
    }

    public SilentWrappedException(@NotNull Throwable cause) {
      super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
      return this;
    }

  }

  public WrappedException(@Nullable String message, @NotNull Throwable cause) {
    super(message, cause);
  }

  public WrappedException(@NotNull Throwable cause) {
    super(cause);
  }

  @NotNull
  @Override
  public Throwable getCause() {
    return super.getCause();
  }

  @NotNull
  public static RuntimeException rethrow(@NotNull Throwable ex) {
    if (ex instanceof Error)
      throw (Error) ex;
    if (ex instanceof RuntimeException)
      throw (RuntimeException) ex;
    throw silent(ex);
  }

  @NotNull
  public static WrappedException silent(@NotNull Throwable cause) {
    return new SilentWrappedException(cause);
  }

  @NotNull
  public static WrappedException silent(@Nullable String message, @NotNull Throwable cause) {
    return new SilentWrappedException(message, cause);
  }

}
