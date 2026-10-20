package net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose;

import org.bukkit.enchantments.Enchantment;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public abstract class CustomChooseMultipleOptionsMenuGenerator extends CustomChooseOptionMenuGenerator {

  public static final int FINISH_SLOT = 40;

  private final Collection<String> selectedKeys = new HashSet<>();

  protected CustomChooseMultipleOptionsMenuGenerator(@NotNull LocalizableMessage menuName, @NotNull Map<String, ? extends SelectableKey> items) {
    super(menuName, items);
  }

  public abstract void onSaveClick(@NotNull Player player, @NotNull String[] keys);

  @Override
  public void onItemClick(@NotNull Player player, @NotNull String key) {
    if (selectedKeys.contains(key)) {
      selectedKeys.remove(key);
    } else {
      selectedKeys.add(key);
    }
  }

  @Override
  public void handleElementClick(@NotNull SelectableKey element, @NotNull MenuClickInfo info) {
    super.handleElementClick(element, info);

    // re-render element
    setElementItemsAt(element, info.getInventory(), info.getSlot(), findLanguageProvider().getPlayerLanguage(info.getPlayer()));
  }

  @NotNull
  @Override
  public ItemStack createDisplayItem(@NotNull SelectableKey element, @NotNull Locale locale) {
    boolean selected = selectedKeys.contains(element.getKey());
    ItemBuilder item = element.getDisplayFormat()
      .suffix(MessageKey.of(selected ? "suffix.selected" : "suffix.unselected"))
      .suffixIfNewUpdated(element)
      .tooltip(MenuClickType.LEFT, MessageKey.of(selected ? "menu.custom.interact.unselect" : "menu.custom.interact.select"))
      .builder(locale);
    if (selected) {
      item.addEnchantment(Enchantment.UNBREAKING, 1);
    }
    return item.build();
  }

  @Override
  public void setInventoryDecoration(@NotNull Inventory inventory, int page, @NotNull Locale locale) {
    super.setInventoryDecoration(inventory, page, locale);
    inventory.setItem(FINISH_SLOT, CustomChooseOptionMenuGenerator.createFinishItem(locale));
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new CustomChooseMultipleOptionsMenuPosition(page, player);
  }

  public class CustomChooseMultipleOptionsMenuPosition extends UncachedMultiPageMenuPosition {

    public CustomChooseMultipleOptionsMenuPosition(int page, @NotNull Player player) {
      super(page, player);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      if (info.getSlot() == FINISH_SLOT) {
        SoundSample.PLOP.play(info.getPlayer());
        onSaveClick(info.getPlayer(), selectedKeys.toArray(new String[0]));
        return true;
      }

      return super.handleMenuClick(info);
    }
  }

}
