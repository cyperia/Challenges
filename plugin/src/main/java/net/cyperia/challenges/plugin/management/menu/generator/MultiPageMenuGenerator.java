package net.cyperia.challenges.plugin.management.menu.generator;

import com.google.common.base.Preconditions;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class MultiPageMenuGenerator extends AbstractMenuGenerator {

  public abstract void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Player viewer);

  public void setInventoryDecoration(@NotNull Inventory inventory, int page, @NotNull Locale locale) {
    MenuBackground.fill(inventory);
  }

  @NotNull
  public Inventory createEmptyInventory(@NotNull Player viewer, int page) {
    MessageKey title = getPageCount(viewer) == 1 ? MessageKey.of("menu.title-format") : MessageKey.of("menu.title-format-page");
    return Bukkit.createInventory(MenuPosition.HOLDER, getInventorySize(),
      title.asComponent(localeOf(viewer), getMenuName(), getMenuTitlePageArg(page)));
  }

  @NotNull
  protected Object getMenuTitlePageArg(int page) {
    return page + 1;
  }

  @NotNull
  @Override
  public final Inventory createInventory(@NotNull Player viewer, int page) {
    int pageCount = getPageCount(viewer);
    Preconditions.checkArgument(page >= 0 && page < pageCount, "Invalid page number: %s; page count: %s", page, pageCount);
    Inventory inventory = createEmptyInventory(viewer, page);
    renderPage(inventory, page, viewer);
    return inventory;
  }

  // The decoration goes first, so a slot whose item is gone since the last render is cleared
  @Override
  public void renderPage(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    setInventoryDecoration(inventory, page, localeOf(viewer));
    setNavigationItems(inventory, page, viewer);
    updateInventoryContent(inventory, page, viewer);
  }

}
