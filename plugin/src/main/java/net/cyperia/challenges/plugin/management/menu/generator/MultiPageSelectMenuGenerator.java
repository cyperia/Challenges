package net.cyperia.challenges.plugin.management.menu.generator;

import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class MultiPageSelectMenuGenerator<T> extends MultiPageMenuGenerator {

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

  public MultiPageSelectMenuGenerator(@NotNull T[] elements) {
    this.elements = elements;
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new MultiPageSelectMenuPosition(page, player);
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    Locale locale = localeOf(viewer);
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
  public int getPageCount(@NotNull Player viewer) {
    return Math.max(1, Math.ceilDiv(elements.length, getMaxEntriesPerPage()));
  }

  public int getMaxEntriesPerPage() {
    return getSlots().length;
  }

  public class MultiPageSelectMenuPosition extends HistoryAwareGeneratorMenuPosition {

    public MultiPageSelectMenuPosition(int page, @NotNull Player player) {
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
