package net.cyperia.commons.database.action;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.Order;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.action.hierarchy.OrderedAction;
import net.cyperia.commons.database.action.hierarchy.WhereAction;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see Database#query(String)
 * @see SpecificDatabase#query()
 */
public interface DatabaseQuery extends DatabaseAction<ExecutedQuery>, WhereAction, OrderedAction {

  @NotNull
  @CheckReturnValue
  DatabaseQuery where(@NotNull String field, @Nullable Object object);

  @NotNull
  @CheckReturnValue
  DatabaseQuery where(@NotNull String field, @Nullable Number value);

  @NotNull
  @CheckReturnValue
  DatabaseQuery where(@NotNull String field, @Nullable String value, boolean ignoreCase);

  @NotNull
  @CheckReturnValue
  DatabaseQuery where(@NotNull String field, @Nullable String value);

  @NotNull
  @CheckReturnValue
  DatabaseQuery whereNot(@NotNull String field, @Nullable Object value);

  @NotNull
  @CheckReturnValue
  DatabaseQuery select(@NotNull String... selection);

  @NotNull
  @CheckReturnValue
  DatabaseQuery orderBy(@NotNull String field, @NotNull Order order);

  @NotNull
  @Override
  ExecutedQuery execute() throws DatabaseException;

}
