package net.cyperia.challenges.plugin.management.menu.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.hook.LanguageProvider;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public abstract class AbstractMenuGenerator implements IMenuGenerator {

  public static final int SHIFT_NAVIGATE_PAGES = 5;

  @Getter
  @Setter
  protected MenuType<?> menuType; // injected by MenuType constructor or set manually

  @Getter
  @Setter
  protected boolean subMenu;

  @NotNull
  public abstract GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player);

  @NotNull
  public abstract Inventory createInventory(@NotNull Player viewer, int page);

  /**
   * Draws a page into an inventory again, the one the viewer has open when a menu changes.
   */
  public abstract void renderPage(@NotNull Inventory inventory, int page, @NotNull Player viewer);

  /**
   * @implSpec multiple of 9
   */
  public abstract int getInventorySize();

  /**
   * @implSpec should not contain any colors by default, inventory title will dynamically be formatted
   */
  @NotNull
  public LocalizableMessage getMenuName() {
    if (menuType == null)
      throw new IllegalStateException("MenuType not set in " + this.getClass() + ", was it not initialized by MenuType directly?");
    return menuType.getMenuName();
  }

  @NotNull
  protected LocalizableMessage createSubMenuTitle(@NotNull Object menuNameArg, @NotNull Object subMenuNameArg) {
    return MessageKey.of("menu.title-name-format-sub").withArgs(menuNameArg, subMenuNameArg);
  }

  @Override
  public void openMenu(@NotNull Player player, int page) {
    openMenuWithPositionOverwrite(player, page, createMenuPosition(page, player));
  }

  /**
   * Opens a page with an already existing position instead of a freshly created one, which keeps the
   * menu history the position carries intact.
   */
  public void openMenuWithPositionOverwrite(@NotNull Player player, int page, @NotNull GeneratorMenuPosition menuPosition) {
    Regions.entity(Challenges.getInstance(), player, () -> {
      Inventory inventory = createInventory(player, page);
      MenuPosition.set(player, menuPosition);
      player.openInventory(inventory);
    });
  }

  protected int getNavigateNextSlot() {
    return getInventorySize() - 1; // bottom right
  }

  protected int getNavigateHomeSlot() {
    return getInventorySize() - 9; // bottom left
  }

  protected int getNavigateBackSlot() {
    return getNavigateHomeSlot() + 1;
  }

  @Override
  public void updatePages() {
    for (Player viewer : retrieveViewers()) {
      int page = openPageOf(viewer);
      int pageCount = getPageCount(viewer);
      if (page < pageCount) {
        renderOpenPage(viewer, page);
      } else {
        openMenu(viewer, pageCount - 1);
      }
    }
  }

  @Override
  public void updatePage(int page) {
    for (Player viewer : retrieveViewers()) {
      if (openPageOf(viewer) == page) {
        renderOpenPage(viewer, page);
      }
    }
  }

  protected void updatePage(int page, @NotNull BiConsumer<Inventory, Locale> updateAction) {
    for (Player viewer : retrieveViewers()) {
      Inventory inventory = openMenuInventory(viewer);
      if (inventory != null && openPageOf(viewer) == page) {
        updateAction.accept(inventory, localeOf(viewer));
      }
    }
  }

  private void renderOpenPage(@NotNull Player viewer, int page) {
    Inventory inventory = openMenuInventory(viewer);
    if (inventory != null) {
      renderPage(inventory, page, viewer);
    }
  }

  @Nullable
  private static Inventory openMenuInventory(@NotNull Player viewer) {
    Inventory inventory = viewer.getOpenInventory().getTopInventory();
    return inventory.getHolder() == MenuPosition.HOLDER ? inventory : null;
  }

  private static int openPageOf(@NotNull Player viewer) {
    return MenuPosition.get(viewer) instanceof GeneratorMenuPosition position ? position.getPage() : -1;
  }

  @NotNull
  protected Locale localeOf(@NotNull Player viewer) {
    return findLanguageProvider().getPlayerLanguage(viewer);
  }

  protected void setNavigationItems(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    Locale locale = localeOf(viewer);
    int lastPage = getPageCount(viewer) - 1;

    if (page < lastPage) {
      boolean canSkipPages = page + SHIFT_NAVIGATE_PAGES <= lastPage;
      inventory.setItem(getNavigateNextSlot(), new ItemFormatter(new ItemBuilder.SkullBuilder().setBase64Texture(DefaultItems.SkullTextures.ARROW_RIGHT))
        .name(MessageKey.of("menu.generic.navigate.next"))
        .tooltip(MenuClickType.LEFT, Interaction.NEXT)
        .tooltipIf(canSkipPages, MenuClickType.LEFT_SHIFT, Interaction.NEXT_SHIFT.withArgs(SHIFT_NAVIGATE_PAGES))
        .tooltipIf(!canSkipPages && page < lastPage - 1, MenuClickType.LEFT_SHIFT, Interaction.NEXT_LAST.withArgs(lastPage + 1))
        .build(locale));
    }

    inventory.setItem(getNavigateHomeSlot(), new ItemFormatter(Material.DARK_OAK_DOOR)
      .name(MessageKey.of("menu.generic.navigate.back_menu"))
      .tooltip(MenuClickType.LEFT, Interaction.BACK_MENU)
      .tooltipIf(isSubMenu(), MenuClickType.LEFT_SHIFT, Interaction.BACK_MAIN_MENU)
      .build(locale));

    if (page > 0) {
      boolean canSkipPages = page - SHIFT_NAVIGATE_PAGES >= 0;
      inventory.setItem(getNavigateBackSlot(), new ItemFormatter(new ItemBuilder.SkullBuilder().setBase64Texture(DefaultItems.SkullTextures.ARROW_LEFT))
        .name(MessageKey.of("menu.generic.navigate.back"))
        .tooltip(MenuClickType.LEFT, Interaction.BACK)
        .tooltipIf(canSkipPages, MenuClickType.LEFT_SHIFT, Interaction.BACK_SHIFT.withArgs(SHIFT_NAVIGATE_PAGES))
        .tooltipIf(!canSkipPages && page > 1, MenuClickType.LEFT_SHIFT, Interaction.BACK_FIRST)
        .build(locale));
    }
  }

  protected boolean isSlotUnoccupied(int page, int slot, @NotNull Player viewer) {
    if (page > 0 && slot == getNavigateBackSlot()) {
      return false;
    }
    if (page < getPageCount(viewer) - 1 && slot == getNavigateNextSlot()) {
      return false;
    }
    return slot != getNavigateHomeSlot();
  }

  protected void setItemSafely(@NotNull Inventory inventory, int slot, @NotNull ItemStack item, int page, @NotNull Player viewer) {
    if (isSlotUnoccupied(page, slot, viewer)) {
      inventory.setItem(slot, item);
    }
  }

  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    Challenges.getInstance().getMenuManager().openMainMenuInstantly(player);
  }

  @NotNull
  protected LanguageProvider findLanguageProvider() {
    return Challenges.getInstance().getTranslationManager().getLanguageProvider();
  }

  @NotNull
  public Collection<Player> retrieveViewers() {
    List<Player> viewers = new ArrayList<>(); // default capacity: 10
    for (Player player : Bukkit.getOnlinePlayers()) {
      MenuPosition position = MenuPosition.get(player);
      if (position instanceof GeneratorMenuPosition generatorPosition && generatorPosition.getGenerator() == this) {
        viewers.add(player);
      }
    }
    return viewers;
  }

  @Getter
  @AllArgsConstructor
  public abstract class GeneratorMenuPosition implements MenuPosition {

    protected final int page;

    @Override
    public void handleClick(@NotNull MenuClickInfo info) {
      int pageCount = getPageCount(info.getPlayer());
      if (page >= pageCount) { // should not happen; dynamically calculated page count changed
        SoundSample.CLICK.play(info.getPlayer());
        return;
      }

      int pageSwitchAmount = info.isShiftClick() ? SHIFT_NAVIGATE_PAGES : 1;
      if (info.getSlot() == getNavigateHomeSlot()) {
        SoundSample.OPEN.play(info.getPlayer());
        if (info.isShiftClick()) {
          Challenges.getInstance().getMenuManager().openMainMenuInstantly(info.getPlayer());
        } else {
          handleNavigateOutOfMenu(info.getPlayer());
        }
        return;
      } else if (info.getSlot() == getNavigateBackSlot()) {
        SoundSample.CLICK.play(info.getPlayer());
        if (page > 0) {
          openMenu(info.getPlayer(), Math.max(page - pageSwitchAmount, 0));
        }
        return;
      } else if (info.getSlot() == getNavigateNextSlot()) {
        SoundSample.CLICK.play(info.getPlayer());
        int maxPageIndex = pageCount - 1;
        if (page < maxPageIndex) {
          openMenu(info.getPlayer(), Math.min(page + pageSwitchAmount, maxPageIndex));
        }
        return;
      }

      if (!handleMenuClick(info)) {
        SoundSample.CLICK.play(info.getPlayer());
      }
    }

    /**
     * @implSpec Return {@code true} if a valid slot handled by this Position was clicked,
     * otherwise return {@code false} to play default fallback behavior (click sound for invalid clicks)
     */
    public abstract boolean handleMenuClick(@NotNull MenuClickInfo info);

    protected void handleNavigateOutOfMenu(@NotNull Player player) {
      AbstractMenuGenerator.this.handleNavigateOutOfMenu(player);
    }

    @NotNull
    public AbstractMenuGenerator getGenerator() {
      return AbstractMenuGenerator.this;
    }

    public void reopen(@NotNull Player player) {
      openMenu(player, page);
    }

  }

  @Getter
  public abstract class HistoryAwareGeneratorMenuPosition extends GeneratorMenuPosition {

    private final GeneratorMenuPosition previousPosition;
    private volatile boolean forgotten = false;

    public HistoryAwareGeneratorMenuPosition(int page, @Nullable GeneratorMenuPosition previousPosition) {
      super(page);
      this.previousPosition = previousPosition;
    }

    public HistoryAwareGeneratorMenuPosition(int page, @NotNull Player player) {
      super(page);
      if (MenuPosition.get(player) instanceof GeneratorMenuPosition prevPosition) {
        while (prevPosition instanceof HistoryAwareGeneratorMenuPosition historyAwarePosition
          && shouldSkipPreviousPosition(historyAwarePosition)) {
          prevPosition = historyAwarePosition.getPreviousPosition();
        }
        this.previousPosition = prevPosition;
      } else {
        this.previousPosition = null;
      }
    }

    protected boolean shouldSkipPreviousPosition(@NotNull HistoryAwareGeneratorMenuPosition prevPosition) {
      // skip other pages of same menu (we only care about the menu before!) + skip all forgotten positions
      return prevPosition.isForgotten()
        || prevPosition.getGenerator() == AbstractMenuGenerator.this
        && prevPosition.getPreviousPosition() != null;
    }

    @Override
    protected void handleNavigateOutOfMenu(@NotNull Player player) {
      this.forgotten = true;

      if (previousPosition == null) {
        super.handleNavigateOutOfMenu(player);
        return;
      }

      AbstractMenuGenerator previousMenu = previousPosition.getGenerator();
      int previousPage = previousPosition.getPage();
      int previousMenuPageCount = previousMenu.getPageCount(player);
      if (previousPage < previousMenuPageCount) { // dynamic page count might have changed
        previousMenu.openMenuWithPositionOverwrite(player, previousPage, previousPosition);
      } else {
        previousMenu.openMenuWithPositionOverwrite(player, previousMenuPageCount - 1, previousPosition);
      }
    }
  }

}
