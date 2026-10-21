package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandProvider;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.spigot.command.ShortcutCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnderChestCommandSetting extends Setting implements CommandProvider {

  @NotNull
  @Override
  public List<ChallengeCommand> getCommands() {
    return List.of(ShortcutCommand.forPlayer(CommandInfo.command("enderchest", ChallengesPermissions.COMMAND_ENDERCHEST).withAliases("ec"),
      (player, context) -> openEnderChest(player)));
  }

  private void openEnderChest(@NotNull Player player) {
    if (!isEnabled() || ChallengeAPI.isWorldInUse()) {
      MessageKey.of("command.feature-disabled").send(player, Prefix.CHALLENGES);
      return;
    }

    player.openInventory(player.getEnderChest());
    getChallengeMessageKey("opened").send(player, Prefix.CHALLENGES);
  }

}
