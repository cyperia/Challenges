package net.cyperia.commons.database.action;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.action.hierarchy.SetAction;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see Database#insert(String)
 * @see SpecificDatabase#insert()
 */
public interface DatabaseInsertion extends DatabaseAction<Void>, SetAction {

  @NotNull
  @CheckReturnValue
  DatabaseInsertion set(@NotNull String field, @Nullable Object value);

  @Nullable
  @Override
  Void execute() throws DatabaseException;

}
