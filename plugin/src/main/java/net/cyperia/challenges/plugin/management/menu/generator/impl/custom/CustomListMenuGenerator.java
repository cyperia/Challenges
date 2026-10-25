package net.cyperia.challenges.plugin.management.menu.generator.impl.custom;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.ChallengeListMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class CustomListMenuGenerator extends ChallengeListMenuGenerator {

  public static final int SIZE = CustomHomeMenuGenerator.SIZE;

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    Challenges.getInstance().getMenuManager().openMenu(player, MenuType.CUSTOM, 0);
  }

  @Override
  public boolean executeChallengeClickAction(@NotNull IChallenge challenge, int slotOffset, @NotNull MenuClickInfo info) {
    if (slotOffset == SLOT_OFFSET_DISPLAY || slotOffset == SLOT_OFFSET_LOWER) {
      challenge.handleClick(new ChallengeMenuClickInfo(info, slotOffset == SLOT_OFFSET_LOWER));
      return true;
    } else if (slotOffset == SLOT_OFFSET_SETTINGS && challenge instanceof CustomChallenge customChallenge) {
      // TODO save & reuse?
      CustomChallengeMenuGenerator generator = new CustomChallengeMenuGenerator(customChallenge);
      generator.openMenu(info.getPlayer());
      SoundSample.CLICK.play(info.getPlayer());
      return true;
    }
    return false;
  }

  @Override
  protected void setChallengeItemsAt(@NotNull IChallenge challenge, @NotNull Inventory inventory, int slotsIndex, @NotNull Locale locale) {
    inventory.setItem(DISPLAY_SLOTS[slotsIndex] + SLOT_OFFSET_DISPLAY, createChallengeDisplayItem(challenge, locale));
    inventory.setItem(DISPLAY_SLOTS[slotsIndex] + SLOT_OFFSET_SETTINGS, createChallengeCustomizeItem(challenge, locale));
    inventory.setItem(DISPLAY_SLOTS[slotsIndex] + SLOT_OFFSET_LOWER, createChallengeSettingsItem(challenge, locale));
  }

  @NotNull
  protected ItemStack createChallengeCustomizeItem(@NotNull IChallenge challenge, @NotNull Locale locale) {
    return new ItemFormatter(DefaultItems.createCustomizePreset())
      .name(MessageKey.of("generic.customize"))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.custom.interact.edit"))
      .build(locale);
  }

  @Override
  protected void setChallengeUpdateItemsAt(@NotNull IChallenge challenge, @NotNull Inventory inventory, int slotIndex, int page, @NotNull Player viewer) {
    // remove update/new indicator items as they would collide with display items and are not appropriate for custom challenges
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }
}
