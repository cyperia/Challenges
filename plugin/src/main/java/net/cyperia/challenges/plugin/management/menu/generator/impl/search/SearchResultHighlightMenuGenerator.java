package net.cyperia.challenges.plugin.management.menu.generator.impl.search;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.menu.generator.AbstractMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.MultiPageMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.ChallengeListMenuGenerator;
import net.cyperia.challenges.plugin.utils.misc.MaterialCategories;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A copy of one page of another menu, flashing the slot a search result sits in. It is a view on top
 * of the menu it was created for: clicks are handed to its position and, once the highlight is over,
 * that very position takes over again, so the detour stays invisible to the menu history.
 */
public class SearchResultHighlightMenuGenerator extends AbstractMenuGenerator {

  public static final int UPDATE_INTERVAL = 10; // in ticks
  public static final int FLASH_INTERVAL_MOD = 1; // every x update intervals
  public static final int MAX_HIGHLIGHT_DURATION = 2 * 5; // in update intervals

  public static final ItemStack HIGHLIGHT_FLASH_ITEM = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE, Component.empty()).build();
  public static final Set<Material> ALLOWED_REPLACEMENTS = Set.of(MaterialCategories.getGlassPanes());

  private final MultiPageMenuGenerator parentGenerator;
  private final int parentPage;
  private final int parentSlot;

  private int durationCounter;
  private boolean flashPresent;

  private Player viewer;
  private Inventory parentInventory;
  private Inventory viewInventory;
  private volatile ScheduledTask updateTask;

  /**
   * @param parentPage the page the result is displayed on
   * @param parentSlot the display slot of the result on that page, the highlight is placed around it
   */
  public SearchResultHighlightMenuGenerator(@NotNull MultiPageMenuGenerator parentGenerator, int parentPage, int parentSlot) {
    this.parentGenerator = parentGenerator;
    this.parentPage = parentPage;
    this.parentSlot = parentSlot;
  }

  @Override
  public void openMenu(@NotNull Player player) {
    openMenu(player, parentPage);
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new SearchResultHighlightMenuPosition(parentGenerator.createMenuPosition(page, player));
  }

  @NotNull
  @Override
  public Inventory createInventory(@NotNull Player viewer, int page) {
    if (viewInventory != null && parentInventory != null) return viewInventory;
    this.viewer = viewer;
    parentInventory = parentGenerator.createInventory(viewer, page);
    viewInventory = parentGenerator.createEmptyInventory(viewer, page); // copied title
    copyContents(parentInventory, viewInventory);
    startUpdateTask();
    return viewInventory;
  }

  @Override
  public void renderPage(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    updateFlashContents();
  }

  @Override
  public int getInventorySize() {
    return parentGenerator.getInventorySize();
  }

  @Override
  public int getPageCount(@NotNull Player viewer) {
    return parentGenerator.getPageCount(viewer);
  }

  private void startUpdateTask() {
    if (updateTask != null) return;
    durationCounter = 0;
    updateTask = Regions.timer(Challenges.getInstance(), viewer, _ -> updateViewCounter(), 1, UPDATE_INTERVAL);
  }

  private void updateViewCounter() {
    // cleanup stale timers
    if (parentInventory == null
      || viewInventory == null
      || viewInventory.getViewers().isEmpty()) {
      kill();
      return;
    }

    if (durationCounter >= MAX_HIGHLIGHT_DURATION) {
      kill();
      restoreParentMenu();
      return;
    }

    durationCounter++;
    if (durationCounter % FLASH_INTERVAL_MOD == 0) {
      flashPresent = !flashPresent;
    }

    updateFlashContents();
  }

  private void restoreParentMenu() {
    for (Player player : retrieveViewers()) {
      if (!(MenuPosition.get(player) instanceof SearchResultHighlightMenuPosition position)) continue;
      parentGenerator.openMenuWithPositionOverwrite(player, parentPage, position.parentPosition);
    }
  }

  // The viewer's position is this highlight, so the parent never updates its copy itself; it is drawn again every flash
  private void updateFlashContents() {
    if (parentInventory == null || viewInventory == null) return;
    parentGenerator.renderPage(parentInventory, parentPage, viewer);
    copyContents(parentInventory, viewInventory);

    if (flashPresent) {
      setHighlightItemSafely(viewInventory, parentSlot + ChallengeListMenuGenerator.SLOT_OFFSET_UPPER, false);
      setHighlightItemSafely(viewInventory, parentSlot + ChallengeListMenuGenerator.SLOT_OFFSET_LOWER, true);
    }
  }

  private void setHighlightItemSafely(@NotNull Inventory inventory, int slot, boolean goDownIfOccupied) {
    ItemStack existing = inventory.getItem(slot);
    while (!isSlotFree(existing)) {
      slot = goDownIfOccupied ? slot + 9 : slot - 9;
      if (slot >= inventory.getSize() || slot < 0) {
        return; // no free slot found
      }
      existing = inventory.getItem(slot);
    }

    inventory.setItem(slot, HIGHLIGHT_FLASH_ITEM);
  }

  private boolean isSlotFree(@Nullable ItemStack occupied) {
    return occupied == null || occupied.isEmpty() || ALLOWED_REPLACEMENTS.contains(occupied.getType());
  }

  private void kill() {
    if (updateTask != null && !updateTask.isCancelled()) {
      updateTask.cancel();
      updateTask = null;
    }

    viewInventory = null;
    parentInventory = null;
    viewer = null;
  }

  private void copyContents(@NotNull Inventory parentInventory, @NotNull Inventory viewInventory) {
    viewInventory.setContents(parentInventory.getContents());
  }

  public class SearchResultHighlightMenuPosition extends GeneratorMenuPosition {

    protected final GeneratorMenuPosition parentPosition;

    public SearchResultHighlightMenuPosition(@NotNull GeneratorMenuPosition parentPosition) {
      super(parentPosition.getPage());
      this.parentPosition = parentPosition;
    }

    @Override
    public void handleClick(@NotNull MenuClickInfo info) {
      parentPosition.handleClick(info);
      updateFlashContents();
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      return false; // unused, as handleClick is overridden directly
    }

    @Override
    public void handleClose(@NotNull Player player) {
      parentPosition.handleClose(player);
      kill();
    }
  }

}
