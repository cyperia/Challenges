package net.cyperia.challenges.plugin.challenges.type.abstraction.menu;

import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.generator.IDynamicMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.MultiPageMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.ChallengeListMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.search.SearchResultHighlightMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubSettingsMenuGenerator extends MultiPageMenuGenerator implements IDynamicMenuGenerator<MenuSetting.SubSetting> {

  public static final int SIZE = ChallengeListMenuGenerator.SIZE;
  public static final int MAX_SLOTS_PER_PAGE = 7;

  public static int[] getSlots(int amount) {
    return switch (amount) {
      case 1 -> new int[]{13};
      case 2 -> new int[]{12, 14};
      case 3 -> new int[]{11, 13, 15};
      case 4 -> new int[]{10, 12, 14, 16};
      case 5 -> new int[]{10, 11, 13, 15, 16};
      case 6 -> new int[]{10, 11, 12, 13, 14, 16};
      case 7 -> new int[]{10, 11, 12, 13, 14, 15, 16};
      default -> new int[0]; // see MAX_SLOTS
    };
  }

  @Getter
  private final MenuSetting challenge;
  private final List<MenuSetting.SubSetting> subSettingsCache = new ArrayList<>(); // order, random access

  public SubSettingsMenuGenerator(@NotNull MenuSetting challenge) {
    this.challenge = challenge;
    setMenuType(challenge.getMenu());
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(challenge.getMenu().getMenuName(), MessageKey.of("challenge." + challenge.getNameMessageKey() + ".menu"));
  }

  @Override
  public void addToCache(@NotNull MenuSetting.SubSetting setting) {
    subSettingsCache.add(setting);
  }

  @Override
  public void removeFromCache(@NotNull MenuSetting.SubSetting setting) {
    subSettingsCache.remove(setting);
  }

  @Override
  public int getCachedCount() {
    return subSettingsCache.size();
  }

  @Override
  public boolean isCached(@NotNull MenuSetting.SubSetting setting) {
    return subSettingsCache.contains(setting);
  }

  @Override
  public void resetCache() {
    subSettingsCache.clear();
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Locale locale) {
    List<MenuSetting.SubSetting> settings = getSubSettingsForPage(page);
    int[] slots = getSlots(settings.size());

    int i = 0;
    for (MenuSetting.SubSetting setting : settings) {
      setSubSettingItemsAt(setting, inventory, slots[i], locale);
      i++;
    }
  }

  @Override
  public void updateElementDisplay(@NotNull MenuSetting.SubSetting setting) {
    int settingsIndex = subSettingsCache.indexOf(setting);
    if (settingsIndex == -1) return; // not cached

    int page = getPageOfSubSetting(settingsIndex);
    int displaySlot = getDisplaySlotOfSubSetting(settingsIndex);

    updatePage(page, (inventory, locale) -> setSubSettingItemsAt(setting, inventory, displaySlot, locale));
  }

  @Override
  public void showFinable(@NotNull Player player, @NotNull MenuSetting.SubSetting setting) {
    int settingsIndex = subSettingsCache.indexOf(setting);
    if (settingsIndex == -1) return; // not cached

    int page = getPageOfSubSetting(settingsIndex);
    int displaySlot = getDisplaySlotOfSubSetting(settingsIndex);

    new SearchResultHighlightMenuGenerator(this, page, displaySlot).openMenu(player);
  }

  public void setSubSettingItemsAt(@NotNull MenuSetting.SubSetting setting, @NotNull Inventory inventory, int displaySlot, @NotNull Locale locale) {
    inventory.setItem(displaySlot + ChallengeListMenuGenerator.SLOT_OFFSET_DISPLAY, setting.getDisplayItem(locale).build());
    inventory.setItem(displaySlot + ChallengeListMenuGenerator.SLOT_OFFSET_SETTINGS, setting.getSettingsItem(locale).build());
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new SubSettingsMenuPosition(page, player);
  }

  @NotNull
  protected List<MenuSetting.SubSetting> getSubSettingsForPage(int page) {
    int startIndex = page * MAX_SLOTS_PER_PAGE;
    int endIndex = Math.min(startIndex + MAX_SLOTS_PER_PAGE, subSettingsCache.size());
    return subSettingsCache.subList(startIndex, endIndex);
  }

  protected int getSubSettingsCountForPage(int page) {
    return Math.min(MAX_SLOTS_PER_PAGE, subSettingsCache.size() - page * MAX_SLOTS_PER_PAGE);
  }

  protected int getPageOfSubSetting(int indexOfSetting) {
    if (indexOfSetting == -1) return -1; // not cached
    return (indexOfSetting / MAX_SLOTS_PER_PAGE);
  }

  protected int getDisplaySlotOfSubSetting(int indexOfSetting) {
    if (indexOfSetting == -1) return -1; // not cached
    int settingsOnPage = getSubSettingsCountForPage(getPageOfSubSetting(indexOfSetting));
    int slotIndex = indexOfSetting % MAX_SLOTS_PER_PAGE; // don't use settingsOnPage here; pages before might have more
    return getSlots(settingsOnPage)[slotIndex];
  }

  @Override
  public int getPageCount() {
    // must have at least one page
    return Math.max(1, Math.ceilDiv(subSettingsCache.size(), MAX_SLOTS_PER_PAGE));
  }

  @Override
  public void openPageOf(@NotNull Player player, @NotNull MenuSetting.SubSetting setting) {
    int indexOfSetting = subSettingsCache.indexOf(setting);
    if (indexOfSetting == -1) return; // not cached

    int page = getPageOfSubSetting(indexOfSetting);
    openMenu(player, page);
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    challenge.getMenu().getMenuGenerator().openPageOf(player, challenge);
  }

  public class SubSettingsMenuPosition extends HistoryAwareGeneratorMenuPosition {

    public SubSettingsMenuPosition(int page, @NotNull Player player) {
      super(page, player);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      List<MenuSetting.SubSetting> settings = getSubSettingsForPage(page);
      int[] slots = getSlots(settings.size());
      for (int i = 0; i < settings.size() && i < slots.length; i++) {
        int slot = slots[i];
        if (info.getSlot() != slot && info.getSlot() != slot + 9) continue;
        MenuSetting.SubSetting setting = settings.get(i);
        if (Challenges.getInstance().getMenuManager().denySettingsChange(info.getPlayer())) return true;
        setting.handleClick(new ChallengeMenuClickInfo(info, info.getSlot() == slot));
        return true;
      }

      return false;
    }
  }

}
