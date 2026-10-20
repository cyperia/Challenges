package net.cyperia.commons.database.action;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.action.hierarchy.SetAction;
import net.cyperia.commons.database.action.hierarchy.WhereAction;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see Database#update(String)
 * @see SpecificDatabase#update()
 */
public interface DatabaseUpdate extends DatabaseAction<Void>, WhereAction, SetAction {

  @NotNull
  @CheckReturnValue
  DatabaseUpdate where(@NotNull String field, @Nullable Object value);

  @NotNull
  @CheckReturnValue
  DatabaseUpdate where(@NotNull String field, @Nullable Number value);

  @NotNull
  @CheckReturnValue
  DatabaseUpdate where(@NotNull String field, @Nullable String value, boolean ignoreCase);

  @NotNull
  @CheckReturnValue
  DatabaseUpdate where(@NotNull String field, @Nullable String value);

  @NotNull
  @CheckReturnValue
  DatabaseUpdate whereNot(@NotNull String field, @Nullable Object value);

  @NotNull
  @CheckReturnValue
  DatabaseUpdate set(@NotNull String field, @Nullable Object value);

  @Nullable
  @Override
  Void execute() throws DatabaseException;

}
