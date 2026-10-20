package net.cyperia.challenges.plugin.management.menu.generator;

import com.google.common.base.Preconditions;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class MultiPageMenuGenerator extends AbstractMenuGenerator {

  // thread-safety: reads/writes only triggered by Player interaction Events on the main thread
  // if any async computation is done, the write access should be handed over to the main thread
  // org.bukkit.Inventory operations are also not thread-safe!
  protected final Map<Locale, List<Inventory>> inventoriesCache = new HashMap<>();

  public abstract void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Locale locale);

  public void setInventoryDecoration(@NotNull Inventory inventory, int page, @NotNull Locale locale) {
    MenuBackground.fill(inventory);
  }

  @NotNull
  public Inventory createEmptyInventory(@NotNull Locale locale, int page) {
    return Bukkit.createInventory(MenuPosition.HOLDER, getInventorySize(),
      getMenuTitleKey().asComponent(locale, getMenuName(), getMenuTitlePageArg(page)));
  }

  @NotNull
  protected Object getMenuTitlePageArg(int page) {
    return page + 1;
  }

  @NotNull
  private MessageKey getMenuTitleKey() {
    if (getPageCount() == 1) return MessageKey.of("menu.title-format");
    return MessageKey.of("menu.title-format-page");
  }

  @NotNull
  @CheckReturnValue
  protected final List<Inventory> generateInventories(@NotNull Locale locale) {
    int pageCount = getPageCount();
    List<Inventory> inventories = new ArrayList<>(pageCount);
    for (int page = 0; page < pageCount; page++) {
      Inventory inventory = createEmptyInventory(locale, page);
      setInventoryDecoration(inventory, page, locale);
      setNavigationItems(inventory, page, locale);
      updateInventoryContent(inventory, page, locale);
      inventories.add(inventory);
    }
    return inventories;
  }

  @NotNull
  @Override
  public final Inventory getOrInitInventory(@NotNull Locale locale, int page) {
    Preconditions.checkArgument(page >= 0 && page < getPageCount(), "Invalid page number: %s; page count: %s", page, getPageCount());
    List<Inventory> inventories = inventoriesCache.computeIfAbsent(locale, this::generateInventories);
    return inventories.get(page);
  }

  @Override
  public final void updatePages() {
    Collection<Locale> locales = new ArrayList<>(inventoriesCache.keySet()); // might get modified
    for (Locale locale : locales) {
      updateOrGeneratePages(locale);
    }
  }

  @Override
  public void updateOrGeneratePages(@NotNull Locale locale) {
    List<Inventory> inventories = inventoriesCache.get(locale);
    if (inventories == null) {
      // locale not generated yet: build every page from scratch
      getOrInitInventory(locale, 0);
      return;
    }

    int pageCount = getPageCount(); // dynamically calculated, might have changed

    // drop surplus pages if the visible item set shrank across a page boundary
    while (inventories.size() > pageCount) {
      removeLastPage(inventories);
    }

    // Re-render every still-existing page. The visible item set may have changed without the page
    // count changing (e.g. a setting hiding itself once a non-default LanguageProvider is installed),
    // so stale slots must be rebuilt in place rather than left untouched. Refilling the decoration
    // first clears slots whose item was removed.
    for (int page = 0; page < inventories.size(); page++) {
      Inventory inventory = inventories.get(page);
      setInventoryDecoration(inventory, page, locale);
      setNavigationItems(inventory, page, locale);
      updateInventoryContent(inventory, page, locale);
    }

    // append missing pages if the visible item set grew across a page boundary
    for (int page = inventories.size(); page < pageCount; page++) {
      Inventory inventory = createEmptyInventory(locale, page);
      setInventoryDecoration(inventory, page, locale);
      setNavigationItems(inventory, page, locale);
      updateInventoryContent(inventory, page, locale);
      inventories.add(inventory);
    }
  }

  private void removeLastPage(@NotNull List<Inventory> inventories) {
    Inventory removed = inventories.removeLast();
    int newLastPage = inventories.size() - 1;
    // relocate anyone viewing the dropped page onto the new last page (or close if none remains)
    for (HumanEntity viewer : new ArrayList<>(removed.getViewers())) {
      if (viewer instanceof Player player && newLastPage >= 0) {
        openMenu(player, newLastPage);
      } else {
        viewer.closeInventory();
      }
    }
  }

  @Override
  public final void updatePage(int page) {
    updatePage(page, (inventory, locale) -> updateInventoryContent(inventory, page, locale));
  }

  protected void updatePage(int page, @NotNull BiConsumer<Inventory, Locale> updateAction) {
    Preconditions.checkArgument(page >= 0 && page < getPageCount(), "Invalid page number: %s; page count: %s", page, getPageCount());
    for (Map.Entry<Locale, List<Inventory>> entry : inventoriesCache.entrySet()) {
      List<Inventory> inventories = entry.getValue();
      updateAction.accept(inventories.get(page), entry.getKey());
    }
  }

}
