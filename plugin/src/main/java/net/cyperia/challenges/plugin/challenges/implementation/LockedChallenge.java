package net.cyperia.challenges.plugin.challenges.implementation;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


/**
 * Stands in for a beta challenge in the public release: shown in its place, playable only on the server it transfers to.
 */
public class LockedChallenge extends Setting implements LockedEntry {


  public LockedChallenge(@NotNull Challenge catalogEntry) {
    super(catalogEntry, false);
  }

  @NotNull
  @Override
  public String getUniqueName() {
    return "locked-" + getNameMessageKey();
  }

  @Override
  public void setEnabled(boolean enabled) {
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    Player player = info.getPlayer();
    SoundSample.CLICK.play(player);
    if (info.getType() != MenuClickType.LEFT) return;

    BetaTransferDialog.show(player, getDisplayItem(), getName(), target -> getMenu().getMenuGenerator().openPageOf(target, this));
  }

  @NotNull
  @Override
  public ItemFormatter getSettingsItem() {
    return new ItemFormatter()
      .item(new ItemStack(Material.ENDER_PEARL))
      .name(MessageKey.of("menu.locked.name").withArgs(BetaTransferDialog.HOST))
      .description(MessageKey.of("menu.locked.desc").withArgs(BetaTransferDialog.HOST))
      .tooltips(getSettingsTooltips());
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    return new InteractionTooltips().on(MenuClickType.LEFT, Interaction.TRANSFER);
  }

}
