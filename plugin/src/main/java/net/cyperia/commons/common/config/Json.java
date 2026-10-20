package net.cyperia.commons.common.config;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @see Document
 */
public interface Json {

  @NotNull
  String toJson();

  @NotNull
  String toPrettyJson();

  @NotNull
  @CheckReturnValue
  static Json empty() {
    return constant("{}", "{}");
  }

  @NotNull
  @CheckReturnValue
  static Json supply(@NotNull Supplier<String> normal, @NotNull Supplier<String> pretty) {
    return new Json() {
      @NotNull
      @Override
      public String toJson() {
        return normal.get();
      }

      @NotNull
      @Override
      public String toPrettyJson() {
        return pretty.get();
      }
    };
  }

  @NotNull
  @CheckReturnValue
  static Json constant(@NotNull String json, @NotNull String prettyJson) {
    return supply(() -> json, () -> prettyJson);
  }

}
