package net.cyperia.challenges.plugin.challenges.custom.settings.sub.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.ValueSetting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BooleanSetting extends ValueSetting {

  public BooleanSetting(@NotNull String key, @NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameSpace) {
    super(key, displayItemPreset, nameSpace);
  }

  @Override
  public String onClick(MenuClickInfo info, String value,
                        int slotIndex) {
    return isEnabled(value) ? "disabled" : "enabled";
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset(@NotNull String value) {
    return isEnabled(value) ? DefaultItems.createEnabledPreset() : DefaultItems.createDisabledPreset();
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingsName(@NotNull String value) {
    return isEnabled(value) ? ChallengeHelper.getChallengeEnabledName() : ChallengeHelper.getChallengeDisabledName();
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips(@NotNull String value) {
    return new InteractionTooltips().on(MenuClickType.LEFT, Interaction.toggle(isEnabled(value)));
  }

  private static boolean isEnabled(@NotNull String value) {
    return value.equals("enabled");
  }

}
