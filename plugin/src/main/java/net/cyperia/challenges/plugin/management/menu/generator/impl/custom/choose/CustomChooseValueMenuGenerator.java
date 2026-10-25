package net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose;

import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.ValueSetting;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.generator.MultiPageSelectMenuGenerator;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * Displays adjustable {@link ValueSetting} rows with a finish button.
 * Replaces the legacy {@code ValueMenuGenerator}.
 */
public abstract class CustomChooseValueMenuGenerator extends MultiPageSelectMenuGenerator<ValueSetting> {

  public static final int SIZE = 4 * 9;
  public static final int[] SLOTS = {10, 11, 12, 13}; // settings item at +9 each
  public static final int FINISH_SLOT = 31;

  private final LocalizableMessage menuName;

  @Getter
  private final Map<ValueSetting, String> settings;

  protected CustomChooseValueMenuGenerator(@NotNull LocalizableMessage menuName, @NotNull Map<ValueSetting, String> settings) {
    super(settings.keySet().toArray(new ValueSetting[0]));
    this.menuType = MenuType.CUSTOM;
    this.menuName = menuName;
    this.settings = settings;
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return menuName;
  }

  public abstract void onSaveItemClick(@NotNull Player player);

  @Override
  protected void setElementItemsAt(@NotNull ValueSetting element, @NotNull Inventory inventory, int slot, @NotNull Locale locale) {
    String value = settings.get(element);
    inventory.setItem(slot, element.getDisplayItem(value, locale).build());
    inventory.setItem(slot + 9, element.getSettingsItem(value, locale).build());
  }

  @NotNull
  @Override
  public ItemStack createDisplayItem(@NotNull ValueSetting element, @NotNull Locale locale) {
    return element.getDisplayItem(settings.get(element), locale).build();
  }

  @Override
  public void handleElementClick(@NotNull ValueSetting element, @NotNull MenuClickInfo info) {
    // handled by the custom menu position (adjust value instead of confirm)
  }

  @Override
  public void setInventoryDecoration(@NotNull Inventory inventory, int page, @NotNull Locale locale) {
    super.setInventoryDecoration(inventory, page, locale);
    inventory.setItem(FINISH_SLOT, CustomChooseOptionMenuGenerator.createFinishItem(locale));
  }

  @Override
  public int[] getSlots() {
    return SLOTS;
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new ChooseValueMenuPosition(page);
  }

  public class ChooseValueMenuPosition extends GeneratorMenuPosition {

    public ChooseValueMenuPosition(int page) {
      super(page);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      if (info.getSlot() == FINISH_SLOT) {
        SoundSample.PLOP.play(info.getPlayer());
        onSaveItemClick(info.getPlayer());
        return true;
      }

      int[] slots = getSlots();
      for (int i = 0; i < slots.length; i++) {
        boolean isDisplaySlot = info.getSlot() == slots[i];
        boolean isSettingsSlot = info.getSlot() == slots[i] + 9;
        if (!isDisplaySlot && !isSettingsSlot) continue;

        int index = getPage() * getMaxEntriesPerPage() + i;
        if (index >= elements.length) return false; // page not full

        ValueSetting setting = elements[index];
        String oldValue = settings.get(setting);
        String newValue = setting.onClick(info, oldValue, isSettingsSlot ? 1 : 0);
        settings.put(setting, newValue);
        SoundSample.CLICK.play(info.getPlayer());
        openMenu(info.getPlayer(), getPage()); // re-render adjusted value
        return true;
      }
      return false;
    }
  }

}
