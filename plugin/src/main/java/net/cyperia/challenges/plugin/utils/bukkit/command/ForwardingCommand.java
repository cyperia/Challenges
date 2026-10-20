package net.cyperia.challenges.plugin.utils.bukkit.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ForwardingCommand implements SenderCommand, TabCompleter {

  private final String forwardCommand;
  private final boolean overrideTab;

  public ForwardingCommand(@NotNull String forwardCommand) {
    this(forwardCommand, true);
  }

  public ForwardingCommand(@NotNull String forwardCommand, boolean overrideTab) {
    this.forwardCommand = forwardCommand;
    this.overrideTab = overrideTab;
  }

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {
    Bukkit.dispatchCommand(sender, forwardCommand + " " + String.join(" ", args));
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    return overrideTab ? new ArrayList<>() : null;
  }

}
