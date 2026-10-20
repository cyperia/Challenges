package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.challenges.type.ISetting;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.common.config.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SettingModifier extends Modifier implements ISetting {

  private boolean enabled;

  public SettingModifier() {
    super(64);
  }

  public SettingModifier(int max) {
    super(max);
  }

  public SettingModifier(int min, int max) {
    super(min, max);
  }

  public SettingModifier(int min, int max, int defaultValue) {
    super(min, max, defaultValue);
  }

  protected SettingModifier(@Nullable Challenge catalogEntry, int min, int max, int defaultValue) {
    super(catalogEntry, min, max, defaultValue);
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    if (info.isUpperItemClick() && info.getType() == MenuClickType.LEFT_SHIFT && isUsesGlobalPunish()) {
      SoundSample.CLICK.play(info.getPlayer());
      ChallengeHelper.openPunishmentMenu(info.getPlayer());
    } else if (info.isUpperItemClick() || !enabled) {
      if (info.getType() != MenuClickType.LEFT) {
        SoundSample.CLICK.play(info.getPlayer());
        return;
      }

      setEnabled(!enabled);
      SoundSample.playStatusSound(info.getPlayer(), enabled);
      playStatusUpdateTitle();
    } else {
      super.handleClick(info);
    }
  }

  @NotNull
  @Override
  protected InteractionTooltips getDisplayTooltips() {
    return new InteractionTooltips().on(MenuClickType.LEFT, Interaction.toggle(enabled));
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    // while disabled the settings item toggles instead of modifying, see #handleClick
    return enabled ? super.getSettingsTooltips() : getDisplayTooltips();
  }

  @Nullable
  @Override
  public LocalizableMessage getSettingsDescription() {
    // the name shows the on/off state, so the value goes into the lore
    return getValueUnit().describe(getValue());
  }

  @Override
  public void restoreDefaults() {
    super.restoreDefaults();
    setEnabled(false);
  }

  @Override
  public boolean isDefaultSettings() {
    // enabled by default no implemented yet: defaults to false
    return !enabled && super.isDefaultSettings();
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return DefaultItems.createEnabledValuePreset(getValue());
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingsName() {
    // a named value replaces the plain on/off state, a counted one goes into the lore instead
    ValueUnit unit = getValueUnit();
    return unit.isNamed() ? unit.name(getValue()) : ChallengeHelper.getChallengeEnabledName();
  }

  @Override
  public final boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    if (this.enabled == enabled) return;
    this.enabled = enabled;

    if (enabled) onEnable();
    else onDisable();

    updateItems();
  }

  @Override
  public void playStatusUpdateTitle() {
    ChallengeHelper.playChallengeToggleTitle(this);
  }

  protected void onEnable() {
  }

  protected void onDisable() {
  }

  @Override
  public void handleShutdown() {
    super.handleShutdown();
    onDisable();
  }

  @Override
  public void writeSettings(@NotNull Document document) {
    super.writeSettings(document);
    document.set("enabled", enabled);
  }

  @Override
  public void loadSettings(@NotNull Document document) {
    super.loadSettings(document);
    setEnabled(document.getBoolean("enabled"));
  }

}
