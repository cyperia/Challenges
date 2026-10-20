package net.cyperia.commons.bukkit.utils.menu;

import net.cyperia.commons.bukkit.utils.menu.positions.EmptyMenuPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@FunctionalInterface
public interface MenuPosition {

  final class Holder {

    private Holder() {
    }

    static final Map<Player, MenuPosition> positions = new ConcurrentHashMap<>();

  }

  InventoryHolder HOLDER = new MenuPositionHolder();

  static void set(@NotNull Player player, @NotNull MenuPosition position) {
    MenuPosition prev = Holder.positions.put(player, position);
    if (prev != null) {
      prev.handleClose(player);
    }
  }

  static void remove(@NotNull Player player) {
    MenuPosition prev = Holder.positions.remove(player);
    if (prev != null) {
      prev.handleClose(player);
    }
  }

  @Nullable
  static MenuPosition get(@NotNull Player player) {
    return Holder.positions.get(player);
  }

  static void setEmpty(@NotNull Player player) {
    set(player, new EmptyMenuPosition());
  }

  void handleClick(@NotNull MenuClickInfo info);

  default void handleClose(@NotNull Player player) {
  }

}
