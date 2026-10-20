package net.cyperia.commons.database.sql.abstraction.where;

import org.jetbrains.annotations.NotNull;

public interface SQLWhere {

  @NotNull
  Object[] getArgs();

  @NotNull
  String getAsSQLString();

}
