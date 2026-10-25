package net.cyperia.challenges.plugin.management.menu.generator;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IMenuGenerator {

  void openMenu(@NotNull Player player, int page);

  default void openMenu(@NotNull Player player) {
    openMenu(player, 0);
  }

  /**
   * @implSpec re-renders the menu for everyone who has it open; a page is rendered for its viewer when it is opened
   */
  void updatePages();

  void updatePage(int page);

  /**
   * @implSpec must be at least {@code 1}; may differ between viewers when entries are hidden from some of them
   */
  int getPageCount(@NotNull Player viewer);

}
