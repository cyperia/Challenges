package net.cyperia.challenges.plugin.management.menu.generator;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IDynamicMenuGenerator<T> extends IMenuGenerator {

  void addToCache(@NotNull T element);

  void removeFromCache(@NotNull T element);

  boolean isCached(@NotNull T element);

  int getCachedCount();

  void resetCache();

  void updateElementDisplay(@NotNull T element);

  void openPageOf(@NotNull Player player, @NotNull T element);

  /**
   * Opens the page of {@code element} and points it out, used to show a search result in its menu.
   *
   * @see net.cyperia.challenges.plugin.management.menu.search.SearchFindable#showFindable(Player)
   */
  void showFinable(@NotNull Player player, @NotNull T element);
}
