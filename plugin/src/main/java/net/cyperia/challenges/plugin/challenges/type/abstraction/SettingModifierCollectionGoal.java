package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.common.config.Document;
import org.jetbrains.annotations.NotNull;

public abstract class SettingModifierCollectionGoal extends ModifierCollectionGoal {

  public SettingModifierCollectionGoal(int min, int max, @NotNull Object... target) {
    super(min, max, target);
  }

  public SettingModifierCollectionGoal(int min, int max, int defaultValue, @NotNull Object... target) {
    super(min, max, defaultValue, target);
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    if (info.isUpperItemClick() || !enabled) {
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
    return new InteractionTooltips()
      .on(MenuClickType.LEFT, isEnabled() ? Interaction.DISABLE : Interaction.ENABLE_GOAL);
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    // while disabled the settings item toggles instead of modifying, see #handleClick
    return enabled ? super.getSettingsTooltips() : getDisplayTooltips();
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
