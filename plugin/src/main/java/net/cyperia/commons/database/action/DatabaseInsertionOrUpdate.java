package net.cyperia.commons.database.action;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see Database#insertOrUpdate(String)
 * @see SpecificDatabase#insertOrUpdate()
 */
public interface DatabaseInsertionOrUpdate extends DatabaseUpdate, DatabaseInsertion {

  @NotNull
  @CheckReturnValue
  DatabaseInsertionOrUpdate where(@NotNull String field, @Nullable Object value);

  @NotNull
  @CheckReturnValue
  DatabaseInsertionOrUpdate where(@NotNull String field, @Nullable Number value);

  @NotNull
  @CheckReturnValue
  DatabaseInsertionOrUpdate where(@NotNull String field, @Nullable String value, boolean ignoreCase);

  @NotNull
  @CheckReturnValue
  DatabaseInsertionOrUpdate where(@NotNull String field, @Nullable String value);

  @NotNull
  @Override
  DatabaseInsertionOrUpdate whereNot(@NotNull String field, @Nullable Object value);

  @NotNull
  @Override
  DatabaseInsertionOrUpdate set(@NotNull String field, @Nullable Object value);

  @Nullable
  @Override
  Void execute() throws DatabaseException;

}
