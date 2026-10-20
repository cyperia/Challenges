package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.CommandHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FeedCommand implements SenderCommand, Completer {

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {

    List<Player> targets = new ArrayList<>();

    if (args.length > 0) {
      targets.addAll(CommandHelper.getPlayers(sender, args[0]));

    } else if (sender instanceof Player) {
      targets.add((Player) sender);
    }

    if (targets.isEmpty()) {
      MessageKey.of("command.no-target").send(sender, Prefix.CHALLENGES);
      return;
    }

    boolean otherPlayers = false;
    for (Player target : targets) {
      target.setFoodLevel(20);
      MessageKey.of("command.feed.done").send(target, Prefix.CHALLENGES);

      if (target != sender)
        otherPlayers = true;

    }

    if (otherPlayers)
      MessageKey.of("command.feed.done-others").send(sender, Prefix.CHALLENGES, targets.size());

  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    return CommandHelper.getCompletions(sender);
  }

}
