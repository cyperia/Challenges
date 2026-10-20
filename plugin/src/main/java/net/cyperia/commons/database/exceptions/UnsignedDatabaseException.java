package net.cyperia.commons.database.exceptions;

import net.cyperia.commons.common.collection.WrappedException;
import net.cyperia.commons.database.action.DatabaseAction;
import org.jetbrains.annotations.NotNull;

/**
 * @see DatabaseException
 * @see DatabaseAction#executeUnsigned()
 */
public class UnsignedDatabaseException extends WrappedException {

  public UnsignedDatabaseException(@NotNull DatabaseException cause) {
    super(cause);
  }

  @NotNull
  @Override
  public DatabaseException getCause() {
    return (DatabaseException) super.getCause();
  }
}
