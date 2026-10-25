package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.jetbrains.annotations.Nullable;
import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IModifier;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.common.config.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class Modifier extends AbstractChallenge implements IModifier {

  private final int max, min;
  private final int defaultValue;
  private int value;
  private ValueUnit valueUnit = ValueUnit.VALUE;

  public Modifier(int max) {
    this(1, max);
  }

  public Modifier(int min, int max) {
    this(min, max, min);
  }

  public Modifier(int min, int max, int defaultValue) {
    this(null, min, max, defaultValue);
  }

  protected Modifier(@Nullable Challenge catalogEntry, int min, int max, int defaultValue) {
    super(catalogEntry);
    if (max < min) throw new IllegalArgumentException("max < min");
    if (min < 0) throw new IllegalArgumentException("min < 0");
    if (defaultValue > max) throw new IllegalArgumentException("defaultValue > max");
    if (defaultValue < min) throw new IllegalArgumentException("defaultValue < min");
    this.max = max;
    this.min = min;
    this.value = defaultValue;
    this.defaultValue = defaultValue;
  }

  /**
   * @implSpec Call from the constructor. The unit is read whenever the item is rendered, so it must be set
   * before the challenge shows up in a menu.
   */
  protected final void setValueUnit(@NotNull ValueUnit valueUnit) {
    this.valueUnit = valueUnit;
  }

  /**
   * A {@link ValueUnit#named(java.util.function.IntFunction) named} unit reading its states from this
   * challenge's own key space, one key per value in order, starting at {@link #getMinValue()}.
   */
  @NotNull
  protected final ValueUnit namedValues(@NotNull String... keySuffixes) {
    if (keySuffixes.length != max - min + 1) {
      throw new IllegalArgumentException("expected " + (max - min + 1) + " keys for values "
        + min + ".." + max + ", got " + keySuffixes.length);
    }
    return ValueUnit.named(value -> getChallengeMessageKey(keySuffixes[value - min]));
  }

  @NotNull
  @Override
  public final ValueUnit getValueUnit() {
    return valueUnit;
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return DefaultItems.createValuePreset(value);
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingsName() {
    return valueUnit.name(value);
  }

  @Override
  public void restoreDefaults() {
    setValue(defaultValue);
  }

  @Override
  public boolean isDefaultSettings() {
    return value == defaultValue;
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
      try {
        if (isEnabled()) onValueChange();
      } catch (Exception exception) {
        Challenges.getInstance().getILogger().error("Error while modifying value of Setting {}", getClass().getSimpleName(), exception);
      }
    });

    updateItems();
  }

  protected final void overwriteValue(int value) {
    this.value = value;
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
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    ChallengeHelper.handleModifierClick(info, this);
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    return ChallengeHelper.createModifierTooltips(this);
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
