package net.cyperia.challenges.plugin.utils.bukkit.command;

import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface SenderCommand extends CommandExecutor {

  @Override
  default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    try {
      onCommand(sender, args);
    } catch (Exception ex) {
      sender.sendMessage(Prefix.FALLBACK + "§cSomething went wrong while executing the command");
      Logger.error("Something went wrong while processing the command '{}'", label, ex);
    }
    return true;
  }

  void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception;

}
