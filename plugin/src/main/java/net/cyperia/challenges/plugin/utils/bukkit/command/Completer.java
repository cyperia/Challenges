package net.cyperia.challenges.plugin.utils.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Completer extends TabCompleter {

  @Override
  @Nullable
  default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return onTabComplete(sender, args);
  }

  @Nullable
  List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}
