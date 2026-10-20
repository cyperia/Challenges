package net.cyperia.challenges.plugin.challenges.custom.settings.sub.impl;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.ValueSetting;
import net.cyperia.challenges.plugin.challenges.type.IModifier;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModifierSetting extends ValueSetting implements IModifier {

  // TODO unify desc/name space with other selectable options!

  private final int min, max;
  private final ValueUnit valueUnit;

  private int tempValue;

  public ModifierSetting(@NotNull String key, @NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameSpace,
                         int min, int max) {
    this(key, displayItemPreset, nameSpace, min, max, ValueUnit.VALUE);
  }

  public ModifierSetting(@NotNull String key, @NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameSpace,
                         int min, int max, @NotNull ValueUnit valueUnit) {
    super(key, displayItemPreset, nameSpace);
    this.min = min;
    this.max = max;
    this.valueUnit = valueUnit;
  }

  @Override
  public String onClick(MenuClickInfo info, String value, int slotIndex) {
    return String.valueOf(withValue(value, () -> {
      ChallengeHelper.handleModifierClick(info, this);
      return tempValue;
    }));
  }

  @Override
  public int getValue() {
    return tempValue;
  }

  @Override
  public void setValue(int value) {
    tempValue = value;
  }

  @Override
  public int getMinValue() {
    return min;
  }

  @Override
  public int getMaxValue() {
    return max;
  }

  @Override
  public void playValueChangeTitle() {
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset(@NotNull String value) {
    int intValue = getIntValue(value);
    return DefaultItems.createValuePreset(intValue);
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingsName(@NotNull String value) {
    return valueUnit.name(getIntValue(value));
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips(@NotNull String value) {
    return withValue(value, () -> ChallengeHelper.createModifierTooltips(this));
  }

  @NotNull
  @Override
  public ValueUnit getValueUnit() {
    return valueUnit;
  }

  /**
   * Runs an {@link IModifier} operation against the value the challenge document holds, which is where a
   * custom sub setting keeps its state instead of in a field.
   */
  private <T> T withValue(@NotNull String value, @NotNull Supplier<T> operation) {
    tempValue = getIntValue(value);
    try {
      return operation.get();
    } finally {
      tempValue = 0;
    }
  }

  public int getIntValue(String value) {

    try {
      return Integer.parseInt(value);
    } catch (Exception exception) {
      Challenges.getInstance().getILogger().severe("Something went wrong while parsing the "
        + "value of subsetting " + getKey() + " with value " + value);
      Challenges.getInstance().getILogger().error("", exception);
    }

    return 0;
  }

}
