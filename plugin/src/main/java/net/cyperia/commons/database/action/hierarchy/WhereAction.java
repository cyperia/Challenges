package net.cyperia.commons.database.action.hierarchy;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WhereAction {

  @NotNull
  @CheckReturnValue
  WhereAction where(@NotNull String field, @Nullable Object value);

  @NotNull
  @CheckReturnValue
  WhereAction where(@NotNull String field, @Nullable Number value);

  @NotNull
  @CheckReturnValue
  WhereAction where(@NotNull String field, @Nullable String value, boolean ignoreCase);

  @NotNull
  @CheckReturnValue
  WhereAction where(@NotNull String field, @Nullable String value);

  @NotNull
  @CheckReturnValue
  WhereAction whereNot(@NotNull String field, @Nullable Object value);

}
