package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.jetbrains.annotations.Nullable;
import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ISetting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.challenges.plugin.spigot.events.SettingToggleEvent;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public abstract class Setting extends AbstractChallenge implements ISetting {

  private final boolean enabledByDefault;
  protected boolean enabled;

  public Setting() {
    this(false);
  }

  public Setting(boolean enabledByDefault) {
    this(null, enabledByDefault);
  }

  protected Setting(@Nullable Challenge catalogEntry, boolean enabledByDefault) {
    super(catalogEntry);
    this.enabledByDefault = enabledByDefault;
    setEnabled(enabledByDefault);
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    if (info.isUpperItemClick() && info.getType() == MenuClickType.LEFT_SHIFT && isUsesGlobalPunish()) {
      SoundSample.CLICK.play(info.getPlayer());
      ChallengeHelper.openPunishmentMenu(info.getPlayer());
      return;
    }

    if (info.getType() != MenuClickType.LEFT) {
      SoundSample.CLICK.play(info.getPlayer());
      return;
    }

    setEnabled(!enabled);
    SoundSample.playStatusSound(info.getPlayer(), enabled);
    playStatusUpdateTitle();
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    return new InteractionTooltips().on(MenuClickType.LEFT, Interaction.toggle(isEnabled()));
  }

  @Override
  public void restoreDefaults() {
    setEnabled(enabledByDefault);
  }

  @Override
  public boolean isDefaultSettings() {
    return enabled == enabledByDefault;
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

    if (isEnabled())
      onDisable();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public final boolean isEnabledByDefault() {
    return enabledByDefault;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (this.enabled == enabled) return;
    this.enabled = enabled;

    // A menu click runs on the clicker's thread, but what a setting does belongs to the global region.
    // A Bukkit event must not be called from the threads settings are loaded on either.
    Regions.global(Challenges.getInstance(), () -> {
      try {
        if (enabled) onEnable();
        else onDisable();
      } catch (Exception exception) {
        Challenges.getInstance().getILogger().error("Error while {} Setting {}", enabled ? "enabling" : "disabling", getClass().getSimpleName(), exception);
      }
      Bukkit.getPluginManager().callEvent(new SettingToggleEvent(this));
    });
    updateItems();
  }

  @Override
  public void loadSettings(@NotNull Document document) {
    setEnabled(document.getBoolean("enabled", enabled));
  }

  @Override
  public void writeSettings(@NotNull Document document) {
    document.set("enabled", enabled);
  }

}
