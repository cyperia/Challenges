package net.cyperia.challenges.plugin.management.menu.generator.impl.randomizer;

import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting.Pairing;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.SettingCategory;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.UncachedMultiPageSelectMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.randomizer.RandomizerPairingsMenuGenerator.Entry;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class RandomizerPairingsMenuGenerator extends UncachedMultiPageSelectMenuGenerator<Entry> {

  private static final int SIZE = 6 * 9;
  private static final int[] SLOTS = calcSlots(4, 7, SIZE);

  private final Object title;

  public RandomizerPairingsMenuGenerator(@NotNull Object title, @NotNull Entry[] entries) {
    super(entries);
    this.title = title;
  }

  @NotNull
  public static RandomizerPairingsMenuGenerator of(@NotNull RandomizerSetting randomizer) {
    Entry[] entries = randomizer.getPairings().stream().map(pairing -> new Entry(randomizer, pairing)).toArray(Entry[]::new);
    return new RandomizerPairingsMenuGenerator(randomizer.getMenuName(), entries);
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(SettingCategory.of("randomizer").getMenuName(), title);
  }

  @Override
  public void handleElementClick(@NotNull Entry element, @NotNull MenuClickInfo info) {
  }

  @NotNull
  @Override
  public ItemStack createDisplayItem(@NotNull Entry element, @NotNull Locale locale) {
    Enum<?> from = element.pairing().from();
    return new ItemFormatter(ItemUtils.getIcon(from))
      .name(MessageKey.of("menu.randomizer.entry").withArgs(from))
      .description(element.randomizer().describe(element.pairing()))
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

  public record Entry(@NotNull RandomizerSetting randomizer, @NotNull Pairing pairing) {
  }

}
