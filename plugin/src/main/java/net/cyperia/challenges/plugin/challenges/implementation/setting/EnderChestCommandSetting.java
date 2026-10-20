package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnderChestCommandSetting extends Setting implements PlayerCommand {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
    if (!isEnabled() || ChallengeAPI.isWorldInUse()) {
      MessageKey.of("command.feature-disabled").send(player, Prefix.CHALLENGES);
      return;
    }

    player.openInventory(player.getEnderChest());
    getChallengeMessageKey("opened").send(player, Prefix.CHALLENGES);
  }

}
