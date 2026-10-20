package net.cyperia.commons.database.action;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.action.hierarchy.WhereAction;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see Database#delete(String)
 * @see SpecificDatabase#delete()
 */
public interface DatabaseDeletion extends DatabaseAction<Void>, WhereAction {

  @NotNull
  @CheckReturnValue
  DatabaseDeletion where(@NotNull String field, @Nullable Object value);

  @NotNull
  @CheckReturnValue
  DatabaseDeletion where(@NotNull String field, @Nullable Number value);

  @NotNull
  @CheckReturnValue
  DatabaseDeletion where(@NotNull String field, @Nullable String value, boolean ignoreCase);

  @NotNull
  @CheckReturnValue
  DatabaseDeletion where(@NotNull String field, @Nullable String value);

  @NotNull
  @CheckReturnValue
  DatabaseDeletion whereNot(@NotNull String field, @Nullable Object value);

  @Nullable
  @Override
  Void execute() throws DatabaseException;

}
