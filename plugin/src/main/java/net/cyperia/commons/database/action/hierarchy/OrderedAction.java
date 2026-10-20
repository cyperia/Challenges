package net.cyperia.commons.database.action.hierarchy;

import net.cyperia.commons.database.Order;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OrderedAction {

  @Nullable
  OrderedAction orderBy(@NotNull String field, @NotNull Order order);

}
