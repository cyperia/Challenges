package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.challenges.plugin.challenges.type.IModifier;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.commons.common.config.Document;
import org.jetbrains.annotations.NotNull;

public abstract class ModifierCollectionGoal extends CollectionGoal implements IModifier {

  private final int max, min;
  private final int defaultValue;
  private int value;
  private ValueUnit valueUnit = ValueUnit.VALUE;

  public ModifierCollectionGoal(int min, int max,
                                @NotNull Object[] target) {
    this(min, max, min, target);
  }

  public ModifierCollectionGoal(int min, int max, int defaultValue,
                                @NotNull Object[] target) {
    super(target);
    if (max < min) throw new IllegalArgumentException("max < min");
    if (min < 0) throw new IllegalArgumentException("min < 0");
    if (defaultValue > max) throw new IllegalArgumentException("defaultValue > max");
    if (defaultValue < min) throw new IllegalArgumentException("defaultValue < min");
    this.max = max;
    this.min = min;
    this.value = defaultValue;
    this.defaultValue = defaultValue;
  }

  @Override
  public void restoreDefaults() {
    super.restoreDefaults();
    setValue(defaultValue);
  }

  @Override
  public boolean isDefaultSettings() {
    return value == defaultValue && super.isDefaultSettings();
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    ChallengeHelper.handleModifierClick(info, this);
  }

  protected final void setValueUnit(@NotNull ValueUnit valueUnit) {
    this.valueUnit = valueUnit;
  }

  @NotNull
  @Override
  public final ValueUnit getValueUnit() {
    return valueUnit;
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingsName() {
    return valueUnit.name(value);
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    return ChallengeHelper.createModifierTooltips(this);
  }

//  @Override // TODO why was this overridden??
//  public boolean isEnabled() {
//    return true;
//  }

  @Override
  public void setEnabled(boolean enabled) {
    if (isEnabled() == enabled) return;
    GoalHelper.handleSetEnabled(this, enabled);
    super.setEnabled(enabled);
  }

  @Override
  public final int getValue() {
    return value;
  }

  @Override
  public void setValue(int value) {
    if (value > max) throw new IllegalArgumentException("value > max");
    if (value < min) throw new IllegalArgumentException("value < min");
    this.value = value;

    Regions.global(Challenges.getInstance(), () -> {
      if (isEnabled()) onValueChange();
    });

    updateItems();
  }

  @Override
  public final int getMaxValue() {
    return max;
  }

  @Override
  public final int getMinValue() {
    return min;
  }

  @Override
  public void playValueChangeTitle() {
    ChallengeHelper.playChallengeValueTitle(this);
  }

  protected void onValueChange() {
  }

  @Override
  public void loadSettings(@NotNull Document document) {
    setValue(document.getInt("value", value));
  }

  @Override
  public void writeSettings(@NotNull Document document) {
    document.set("value", value);
  }

}
