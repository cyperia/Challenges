package net.cyperia.challenges.plugin.management.menu.generator.impl.randomizer;

import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.SettingCategory;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.generator.UncachedMultiPageSelectMenuGenerator;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class RandomizerMenuGenerator extends UncachedMultiPageSelectMenuGenerator<RandomizerSetting> {

  private static final int SIZE = 3 * 9;
  private static final int[] SLOTS = calcSlots(1, 7, SIZE);

  public RandomizerMenuGenerator(@NotNull RandomizerSetting[] randomizers) {
    super(randomizers);
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return SettingCategory.of("randomizer").getMenuName();
  }

  @Override
  public void handleElementClick(@NotNull RandomizerSetting element, @NotNull MenuClickInfo info) {
    RandomizerPairingsMenuGenerator.of(element).openMenu(info.getPlayer(), 0);
  }

  @NotNull
  @Override
  public ItemStack createDisplayItem(@NotNull RandomizerSetting element, @NotNull Locale locale) {
    return element.getDisplayItem()
      .tooltips(null)
      .tooltip(MenuClickType.LEFT, Interaction.OPEN)
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
