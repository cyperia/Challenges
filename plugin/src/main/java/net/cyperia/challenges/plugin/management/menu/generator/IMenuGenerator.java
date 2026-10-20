package net.cyperia.challenges.plugin.management.menu.generator;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface IMenuGenerator {

  void openMenu(@NotNull Player player, int page);

  default void openMenu(@NotNull Player player) {
    openMenu(player, 0);
  }

  void updateOrGeneratePages(@NotNull Locale locale);

  /**
   * @implSpec regenerates all pages (only for locales already generated!)
   * use {@link #updateOrGeneratePages(Locale)} to create the menu for a new locale
   */
  void updatePages();

  void updatePage(int page);

  /**
   * @implSpec must be at least {@code 1}
   */
  int getPageCount();

}
