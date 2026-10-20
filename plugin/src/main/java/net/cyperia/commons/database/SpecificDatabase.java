package net.cyperia.commons.database;

import net.cyperia.commons.database.action.*;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a table/collection of a database
 *
 * @see Database
 * @see Database#getSpecificDatabase(String)
 */
public interface SpecificDatabase {

  boolean isConnected();

  @NotNull
  String getName();

  /**
   * @see Database#countEntries(String)
   */
  @NotNull
  @CheckReturnValue
  DatabaseCountEntries countEntries();

  /**
   * @see Database#query(String)
   */
  @NotNull
  @CheckReturnValue
  DatabaseQuery query();

  /**
   * @see Database#update(String)
   */
  @NotNull
  @CheckReturnValue
  DatabaseUpdate update();

  /**
   * @see Database#insert(String)
   */
  @NotNull
  @CheckReturnValue
  DatabaseInsertion insert();

  /**
   * @see Database#insertOrUpdate(String)
   */
  @NotNull
  @CheckReturnValue
  DatabaseInsertionOrUpdate insertOrUpdate();

  /**
   * @see Database#delete(String)
   */
  @NotNull
  @CheckReturnValue
  DatabaseDeletion delete();

  @NotNull
  Database getParent();

}
