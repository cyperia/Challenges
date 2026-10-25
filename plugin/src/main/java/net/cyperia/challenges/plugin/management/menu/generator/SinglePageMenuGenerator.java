package net.cyperia.challenges.plugin.management.menu.generator;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class SinglePageMenuGenerator extends AbstractMenuGenerator {

  @NotNull
  public abstract GeneratorMenuPosition createMenuPosition(@NotNull Player player);

  public void setInventoryDecoration(@NotNull Inventory inventory, @NotNull Locale locale) {
    MenuBackground.fill(inventory);
  }

  public abstract void updateInventoryContent(@NotNull Inventory inventory, @NotNull Locale locale);

  @NotNull
  @Override
  public final GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return createMenuPosition(player); // ignore page; single page
  }

  @NotNull
  @Override
  public final Inventory createInventory(@NotNull Player viewer, int page) {
    Inventory inventory = Bukkit.createInventory(MenuPosition.HOLDER, getInventorySize(),
      MessageKey.of("menu.title-format").asComponent(localeOf(viewer), getMenuName()));
    renderPage(inventory, 0, viewer);
    return inventory;
  }

  @Override
  public void renderPage(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    Locale locale = localeOf(viewer);
    setInventoryDecoration(inventory, locale);
    setNavigationItems(inventory, 0, viewer);
    updateInventoryContent(inventory, locale);
  }

  @Override
  public final int getPageCount(@NotNull Player viewer) {
    return 1;
  }

  public abstract class SinglePageGeneratorMenuPosition extends GeneratorMenuPosition {

    public SinglePageGeneratorMenuPosition() {
      super(0);
    }

  }

}
