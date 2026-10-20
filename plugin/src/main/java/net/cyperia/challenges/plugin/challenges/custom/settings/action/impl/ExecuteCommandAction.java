package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ExecuteCommandAction extends PlayerTargetAction {

  // Static because cannot be accessed before super has been called
  public static int maxCommandLength = Challenges.getInstance().getConfigDocument().getInt(ConfigManager.Keys.CUSTOM_CHALLENGE_MAX_COMMAND_LENGTH);
  private static List<String> commandsThatCanBeExecuted = Challenges.getInstance().getConfigDocument().getStringList(ConfigManager.Keys.CUSTOM_CHALLENGE_ALLOWED_COMMANDS);

  public ExecuteCommandAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true, true, true).createTextInputChild("command", player -> {
      MessageKey.of("custom.action.command.chat-info").send(player, Prefix.CUSTOM, "/" + String.join(" /", commandsThatCanBeExecuted));
    }, (player, input) -> {
      if (!isAllowed(input)) {
        MessageKey.of("custom.action.command.not-allowed").send(player, Prefix.CUSTOM, commandName(input));
        return false;
      }

      if (input.length() > maxCommandLength) {
        MessageKey.of("custom.chars-exceeded").send(player, Prefix.CUSTOM, maxCommandLength);
        return false;
      }
      return true;
    }));
    // Reload from config on reload
    maxCommandLength = Challenges.getInstance().getConfigDocument().getInt(ConfigManager.Keys.CUSTOM_CHALLENGE_MAX_COMMAND_LENGTH);
    commandsThatCanBeExecuted = Challenges.getInstance().getConfigDocument().getStringList(ConfigManager.Keys.CUSTOM_CHALLENGE_ALLOWED_COMMANDS);
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    String command = subActions.get("command")[0];

    // Saved challenges outlive the config they were created under, so the allowlist has to hold here too
    if (!isAllowed(command)) return;

    String fullCommand = player == null
      ? command
      : "execute as " + player.getName() + " at " + player.getName() + " run " + command;

    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), fullCommand);
  }

  private static String commandName(String input) {
    return input.split(" ")[0].toLowerCase();
  }

  private static boolean isAllowed(String input) {
    return commandsThatCanBeExecuted.contains(commandName(input));
  }

}
