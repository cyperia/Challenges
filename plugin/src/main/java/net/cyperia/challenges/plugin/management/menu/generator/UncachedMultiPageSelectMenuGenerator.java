package net.cyperia.challenges.plugin.management.menu.generator;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class UncachedMultiPageSelectMenuGenerator<T> extends AbstractMenuGenerator {

  public static int[] calcSlots(int rows, int cols, int size) {
    if (rows * cols > size)
      throw new IllegalArgumentException("rows (" + rows + ") * cols (" + cols + ") must be <= size (" + size + ")");

    final int sizeRows = size / 9;
    final int rowOffset = Math.floorDiv(sizeRows - rows, 2);
    final int colOffset = Math.floorDiv(9 - cols, 2);

    int[] slots = new int[rows * cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        slots[i * cols + j] = (i + rowOffset) * 9 + (j + colOffset);
      }
    }
    return slots;
  }

  public static int[] calcSlots(int padding, int size) {
    int rows = size / 9;
    int cols = size / rows;
    return calcSlots(rows - 2 * padding, cols - 2 * padding, size);
  }

  protected final T[] elements;

  public UncachedMultiPageSelectMenuGenerator(@NotNull T[] elements) {
    this.elements = elements;
  }

  @Override
  public void updateOrGeneratePages(@NotNull Locale locale) {
    // because pages are not cached, there is noting to update or pregenerate
  }

  @Override
  public void updatePages() {
    // because pages are not cached, there is noting to update
  }

  @Override
  public void updatePage(int page) {
    // because pages are not cached, there is noting to update
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new UncachedMultiPageMenuPosition(page, player);
  }

  @NotNull
  @Override
  public Inventory getOrInitInventory(@NotNull Locale locale, int page) {
    Inventory inventory = createEmptyInventory(locale, page);
    setInventoryDecoration(inventory, page, locale);
    setNavigationItems(inventory, page, locale);
    updateInventoryContent(inventory, page, locale);
    return inventory;
  }

  public void setInventoryDecoration(@NotNull Inventory inventory, int page, @NotNull Locale locale) {
    MenuBackground.fill(inventory);
  }

  @NotNull
  protected Inventory createEmptyInventory(@NotNull Locale locale, int page) {
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

  public void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Locale locale) {
    int startIndex = page * getMaxEntriesPerPage();
    int endIndex = Math.min(startIndex + getMaxEntriesPerPage(), elements.length);
    int[] slots = getSlots();
    for (int i = startIndex; i < endIndex; i++) {
      T element = elements[i];
      setElementItemsAt(element, inventory, slots[i - startIndex], locale);
    }
  }

  protected void setElementItemsAt(@NotNull T element, @NotNull Inventory inventory, int slot, @NotNull Locale locale) {
    ItemStack displayItem = createDisplayItem(element, locale);
    inventory.setItem(slot, displayItem);
  }

  public abstract void handleElementClick(@NotNull T element, @NotNull MenuClickInfo info);

  @NotNull
  public abstract ItemStack createDisplayItem(@NotNull T element, @NotNull Locale locale);

  public abstract int[] getSlots();

  @Override
  public int getPageCount() {
    return Math.ceilDiv(elements.length, getMaxEntriesPerPage());
  }

  public int getMaxEntriesPerPage() {
    return getSlots().length;
  }

  public class UncachedMultiPageMenuPosition extends HistoryAwareGeneratorMenuPosition {

    public UncachedMultiPageMenuPosition(int page, @NotNull Player player) {
      super(page, player);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      int[] slots = getSlots();
      for (int i = 0; i < slots.length; i++) {
        if (info.getSlot() != slots[i]) continue;

        int elementIndex = getPage() * getMaxEntriesPerPage() + i;
        if (elementIndex >= elements.length) return false;

        T element = elements[elementIndex];
        SoundSample.PLOP.play(info.getPlayer());
        handleElementClick(element, info);
        return true;
      }

      return false;
    }
  }

}
