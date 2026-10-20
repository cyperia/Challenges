package net.cyperia.challenges.plugin.management.menu.generator.impl.custom;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.types.IChallengesMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.SinglePageMenuGenerator;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class CustomHomeMenuGenerator extends SinglePageMenuGenerator implements IChallengesMenuGenerator {

  public static final int VIEW_SLOT = 21;
  public static final int CREATE_SLOT = 23;
  public static final int SIZE = 5 * 9;

  private final CustomListMenuGenerator listGenerator = new CustomListMenuGenerator();

  @NotNull
  public CustomListMenuGenerator getListGenerator() {
    return listGenerator;
  }

  @Override
  public void setMenuType(@NotNull MenuType<?> menuType) {
    // MenuType.CUSTOM instance not initialized when creating CustomListMenuGenerator
    super.setMenuType(menuType);
    listGenerator.setMenuType(menuType);
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(@NotNull Player player) {
    return new CustomMainMenuPosition();
  }

  @Override
  public void setInventoryDecoration(@NotNull Inventory inventory, @NotNull Locale locale) {
    InventoryUtils.fillInventory(inventory, DefaultItems.FILL_ITEM); // its own contrast pattern replaces the corner glass
    for (int i : new int[]{1, 2, 6, 7, 9, 10, 16, 17, 27, 28, 34, 35, 37, 38, 39, 41, 42, 43}) {
      inventory.setItem(i, DefaultItems.FILL_ITEM_CONTRAST);
    }
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, @NotNull Locale locale) {
    inventory.setItem(VIEW_SLOT, new ItemFormatter(Material.BOOK)
      .name(MessageKey.of("menu.custom.view.name"))
      .description(MessageKey.of("menu.custom.view.desc"))
      .tooltip(MenuClickType.LEFT, Interaction.OPEN)
      .build(locale));
    inventory.setItem(CREATE_SLOT, new ItemFormatter(Material.WRITABLE_BOOK)
      .name(MessageKey.of("menu.custom.create.name"))
      .description(MessageKey.of("menu.custom.create.desc"))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.custom.interact.create"))
      .build(locale));
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  @Override
  public void addToCache(@NotNull IChallenge element) {
    listGenerator.addToCache(element);
  }

  @Override
  public void removeFromCache(@NotNull IChallenge element) {
    listGenerator.removeFromCache(element);
  }

  @Override
  public boolean isCached(@NotNull IChallenge element) {
    return listGenerator.isCached(element);
  }

  @Override
  public int getCachedCount() {
    return listGenerator.getCachedCount();
  }

  @Override
  public void resetCache() {
    listGenerator.resetCache();
  }

  @Override
  public void updateElementDisplay(@NotNull IChallenge element) {
    listGenerator.updateElementDisplay(element);
  }

  @Override
  public void openPageOf(@NotNull Player player, @NotNull IChallenge challenge) {
    listGenerator.openPageOf(player, challenge);
  }

  @Override
  public void showFinable(@NotNull Player player, @NotNull IChallenge challenge) {
    listGenerator.showFinable(player, challenge);
  }

  public class CustomMainMenuPosition extends SinglePageGeneratorMenuPosition {

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      if (info.getSlot() == VIEW_SLOT) {
        if (Challenges.getInstance().getCustomChallengesLoader().getCustomChallenges().isEmpty()) {
          MessageKey.of("menu.custom.view.empty").send(info.getPlayer(), Prefix.CUSTOM);
          SoundSample.BASS_OFF.play(info.getPlayer());
          return true;
        }
        listGenerator.openMenu(info.getPlayer());
        SoundSample.PLOP.play(info.getPlayer());
        return true;
      } else if (info.getSlot() == CREATE_SLOT) {
        // TODO check limit, abstract logic
        if (Challenges.getInstance().getMenuManager().denySettingsChange(info.getPlayer())) return true;
        CustomChallengeMenuGenerator generator = new CustomChallengeMenuGenerator();
        generator.openMenu(info.getPlayer());
        SoundSample.PLING.play(info.getPlayer());
        return true;
      }

      return false;
    }
  }

}
