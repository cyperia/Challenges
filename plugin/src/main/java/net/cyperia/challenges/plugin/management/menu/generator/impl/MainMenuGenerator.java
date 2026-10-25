package net.cyperia.challenges.plugin.management.menu.generator.impl;

import org.bukkit.entity.Player;
import java.util.stream.Collectors;
import net.cyperia.challenges.plugin.challenges.type.EntryAccess;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.IMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.SingleAnimatedMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.ChallengesMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.CustomHomeMenuGenerator;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.challenges.plugin.utils.misc.GuiSlot;
import net.cyperia.commons.bukkit.utils.animation.AnimatedInventory;
import net.cyperia.commons.bukkit.utils.animation.AnimationFrame;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MainMenuGenerator extends SingleAnimatedMenuGenerator {

  public static final int SIZE = 5 * 9;
  public static final int[] GUI_SLOTS = {
    GuiSlot.at(3, 2), GuiSlot.at(3, 6),
    GuiSlot.at(3, 3), GuiSlot.at(3, 5),
    GuiSlot.at(1, 3), GuiSlot.at(1, 5)
  };
  public static final MenuType<?>[] SLOT_MAPPING = {
    MenuType.CHALLENGES, MenuType.CUSTOM,
    MenuType.GOAL, MenuType.SETTINGS,
    MenuType.SEARCH, MenuType.TIMER
  };

  public MainMenuGenerator() {
    super(Challenges.getInstance().getConfigDocument().getBoolean(ConfigManager.Keys.ANIMATE_MAIN_MENU));
  }

  @NotNull
  @Override
  public MenuPosition createMenuPosition() {
    return new MainMenuPosition();
  }

  @Override
  @NotNull
  public LocalizableMessage getMenuName() {
    return MessageKey.of("menu.main.menu");
  }

  @NotNull
  @Override
  public AnimatedInventory createAnimatedInventory(@NotNull Locale locale, @NotNull Player viewer) {
    AnimatedInventory gui = createEmptyInventory(locale);
    gui.createAndAdd().fill(DefaultItems.FILL_ITEM);
    gui.cloneLastAndAdd().setContrast(
      GuiSlot.at(1, 0), GuiSlot.at(1, 8),
      GuiSlot.at(3, 0), GuiSlot.at(3, 8)
    );
    gui.cloneLastAndAdd().setContrast(
      GuiSlot.at(1, 1), GuiSlot.at(1, 7),
      GuiSlot.at(3, 1), GuiSlot.at(3, 7)
    );
    gui.cloneLastAndAdd().setContrast(
      GuiSlot.at(0, 1), GuiSlot.at(0, 7),
      GuiSlot.at(4, 1), GuiSlot.at(4, 7)
    );
    gui.cloneLastAndAdd().setContrast(
      GuiSlot.at(0, 2), GuiSlot.at(0, 6),
      GuiSlot.at(4, 2), GuiSlot.at(4, 6)
    );
    gui.cloneLastAndAdd().setContrast(
      GuiSlot.at(4, 3), GuiSlot.at(4, 5)
    );

    for (int i = 0; i < SLOT_MAPPING.length; i += 2) {
      AnimationFrame frame = gui.cloneLastAndAdd();

      frame.setItem(GUI_SLOTS[i], createDisplayItem(locale, viewer, SLOT_MAPPING[i]));
      frame.setItem(GUI_SLOTS[i + 1], createDisplayItem(locale, viewer, SLOT_MAPPING[i + 1]));
    }

    return gui;
  }

  @NotNull
  protected ItemStack createDisplayItem(@NotNull Locale locale, @NotNull Player viewer, @NotNull MenuType<?> menuType) {
    ItemFormatter item = new ItemFormatter(menuType.getDisplayItemMaterial())
      .name(menuType.getName())
      .description(menuType.getDescription())
      .suffixIfNewUpdated(menuType.getMenuGenerator(), viewer)
      .tooltip(MenuClickType.LEFT, Interaction.OPEN);

    ChallengesMenuGenerator generator = findChallengesGenerator(menuType);
    if (generator != null) {
      item.state(generator.formatInfo(MessageKey.of(menuType.getMessageKey() + ".state")));
    }

    return item.build(locale);
  }

  @Nullable
  private static ChallengesMenuGenerator findChallengesGenerator(@NotNull MenuType<?> menuType) {
    IMenuGenerator generator = menuType.getMenuGenerator();
    if (generator instanceof CustomHomeMenuGenerator custom) return custom.getListGenerator();
    return generator instanceof ChallengesMenuGenerator challenges ? challenges : null;
  }

  // Who may see a hidden entry sees it in the counts on the menu items
  @NotNull
  @Override
  protected Object variantOf(@NotNull Player viewer) {
    return Challenges.getInstance().getChallengeManager().getChallenges().stream()
      .filter(challenge -> EntryAccess.isHidden(challenge) && EntryAccess.isListed(challenge, viewer))
      .collect(Collectors.toSet());
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  public class MainMenuPosition implements MenuPosition {

    @Override
    public void handleClick(@NotNull MenuClickInfo info) {
      SoundSample.CLICK.play(info.getPlayer());

      for (int i = 0; i < GUI_SLOTS.length; i++) {
        int current = GUI_SLOTS[i];
        if (current == info.getSlot()) {
          Challenges.getInstance().getMenuManager().openMenu(info.getPlayer(), SLOT_MAPPING[i], 0);
          return;
        }
      }
    }

  }
}
