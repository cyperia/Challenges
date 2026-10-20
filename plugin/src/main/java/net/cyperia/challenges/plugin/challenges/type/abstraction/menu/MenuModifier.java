package net.cyperia.challenges.plugin.challenges.type.abstraction.menu;

import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.jetbrains.annotations.NotNull;

public abstract class MenuModifier extends MenuSetting {

  public MenuModifier() {
    super(true);
  }

  @Override
  public final boolean isEnabled() {
    return super.isEnabled();
  }

  @NotNull
  @Override
  protected InteractionTooltips getDisplayTooltips() {
    return getSettingsTooltips();
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    return new InteractionTooltips().on(MenuClickType.LEFT, Interaction.OPEN);
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    if (info.isUpperItemClick() && info.getType() == MenuClickType.LEFT_SHIFT && isUsesGlobalPunish()) {
      SoundSample.CLICK.play(info.getPlayer());
      ChallengeHelper.openPunishmentMenu(info.getPlayer());
      return;
    }

    SoundSample.CLICK.play(info.getPlayer());
    openMenu(info);
  }
}
