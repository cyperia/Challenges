package net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose;

import java.util.Arrays;
import net.cyperia.challenges.plugin.challenges.custom.settings.IChallengeSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.implementation.BetaTransferDialog;
import net.cyperia.challenges.plugin.challenges.implementation.LockedEntry;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.UncachedMultiPageSelectMenuGenerator;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public abstract class CustomChooseOptionMenuGenerator extends UncachedMultiPageSelectMenuGenerator<SelectableKey> {

  public static final int SIZE = 5 * 9;
  public static final int[] SLOTS = calcSlots(3, 7, SIZE);

  private final LocalizableMessage menuName;

  protected CustomChooseOptionMenuGenerator(@NotNull LocalizableMessage menuName, @NotNull Map<String, ? extends SelectableKey> items) {
    super(items.values().toArray(new SelectableKey[0]));
    this.menuType = MenuType.CUSTOM;
    this.menuName = menuName;
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return menuName;
  }

  /**
   * The button confirming a selection that is not applied by clicking a single option.
   */
  @NotNull
  public static ItemStack createFinishItem(@NotNull Locale locale) {
    return new ItemFormatter(Material.LIME_DYE)
      .name(MessageKey.of("menu.custom.finish.name"))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.custom.interact.finish"))
      .build(locale);
  }

  public abstract void onItemClick(@NotNull Player player, @NotNull String key);

  @Override
  public void handleElementClick(@NotNull SelectableKey element, @NotNull MenuClickInfo info) {
    if (element instanceof LockedEntry && element instanceof IChallengeSetting setting) {
      int page = Arrays.asList(elements).indexOf(element) / getMaxEntriesPerPage();
      BetaTransferDialog.show(info.getPlayer(), setting.getDisplayFormat(), setting.getSettingName(), player -> openMenu(player, page));
      return;
    }
    onItemClick(info.getPlayer(), element.getKey());
  }

  @NotNull
  @Override
  public ItemStack createDisplayItem(@NotNull SelectableKey element, @NotNull Locale locale) {
    return element.getDisplayFormat()
      .suffixIfNewUpdated(element)
      .tooltip(MenuClickType.LEFT, element instanceof LockedEntry ? Interaction.TRANSFER : MessageKey.of("menu.custom.interact.select"))
      .build(locale);
  }

  @Override
  public int[] getSlots() {
    return SLOTS;
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

}
