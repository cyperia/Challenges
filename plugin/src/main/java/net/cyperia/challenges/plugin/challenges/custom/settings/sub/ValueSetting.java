package net.cyperia.challenges.plugin.challenges.custom.settings.sub;

import lombok.Getter;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class ValueSetting {

  @Getter
  private final String key;
  private final ItemStack displayItemPreset;
  private final MessageKeySpace messageKeyNamespace;

  public ValueSetting(@NotNull String key, @NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace messageKeyNamespace) {
    this.key = key;
    this.displayItemPreset = displayItemPreset;
    this.messageKeyNamespace = messageKeyNamespace;
  }

  public abstract String onClick(MenuClickInfo info, String value, int slotIndex);

  @NotNull
  public ItemBuilder getDisplayItem(@NotNull String value, @NotNull Locale locale) {
    return new ItemFormatter(displayItemPreset)
      .name(getValueSettingName())
      .description(getValueSettingDescription())
      .builder(locale);
  }

  @NotNull
  public ItemBuilder getSettingsItem(@NotNull String value, @NotNull Locale locale) {
    return new ItemFormatter(getSettingsItemPreset(value))
      .name(getSettingsName(value))
      .tooltips(getSettingsTooltips(value))
      .builder(locale);
  }

  @NotNull
  public abstract ItemStack getSettingsItemPreset(@NotNull String value);

  @NotNull
  protected abstract InteractionTooltips getSettingsTooltips(@NotNull String value);

  @NotNull
  public LocalizableMessage getSubName() {
    return messageKeyNamespace.getChildKey("label");
  }

  @NotNull
  public LocalizableMessage getValueSettingName() {
    return messageKeyNamespace.getChildKey("name");
  }

  @Nullable
  public LocalizableMessage getValueSettingDescription() {
    return messageKeyNamespace.getChildKey("desc");
  }

  @NotNull
  public abstract LocalizableMessage getSettingsName(@NotNull String value);

}
