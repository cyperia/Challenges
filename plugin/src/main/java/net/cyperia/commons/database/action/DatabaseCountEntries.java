package net.cyperia.commons.database.action;

import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.SpecificDatabase;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.jetbrains.annotations.NotNull;

/**
 * @see Database#countEntries(String)
 * @see SpecificDatabase#countEntries()
 */
public interface DatabaseCountEntries extends DatabaseAction<Long> {

  @NotNull
  @Override
  Long execute() throws DatabaseException;

}
