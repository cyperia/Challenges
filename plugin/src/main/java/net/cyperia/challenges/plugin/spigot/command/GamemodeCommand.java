package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.CommandHelper;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GamemodeCommand implements SenderCommand, Completer {

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {
    List<Player> targets = new ArrayList<>();

    if (args.length == 0) {
      MessageKey.of("command.syntax").send(sender, Prefix.CHALLENGES, "gm <gamemode> [player]");
      return;
    }

    GameMode gamemode = getGameMode(args[0]);

    if (gamemode == null) {
      MessageKey.of("command.syntax").send(sender, Prefix.CHALLENGES, "gm <gamemode> [player]");
      return;
    }

    if (!sender.hasPermission(getPermission(gamemode))) {
      MessageKey.of("command.no-permission").send(sender, Prefix.CHALLENGES);
      return;
    }

    if (args.length > 1) {
      targets.addAll(CommandHelper.getPlayers(sender, args[1]));

    } else if (sender instanceof Player) {
      targets.add(((Player) sender).getPlayer());
    }

    if (targets.isEmpty()) {
      MessageKey.of("command.no-target").send(sender, Prefix.CHALLENGES);
      return;
    }

    boolean otherPlayers = false;

    for (Player player : targets) {
      MessageKey.of("command.gamemode.done").send(player, Prefix.CHALLENGES, gamemode);
      player.setGameMode(gamemode);
      if (player != sender)
        otherPlayers = true;

    }
    if (otherPlayers) {
      MessageKey.of("command.gamemode.done-others").send(sender, Prefix.CHALLENGES, gamemode, targets.size());
    }

  }

  @NotNull
  public static String getPermission(@NotNull GameMode gamemode) {
    return "challenges.gamemode." + gamemode.name().toLowerCase();
  }

  @Nullable
  private GameMode getGameMode(String input) {
    switch (input.toLowerCase()) {
      case "survival":
      case "s":
      case "0":
        return GameMode.SURVIVAL;
      case "spectator":
      case "sp":
      case "3":
        return GameMode.SPECTATOR;
      case "adventure":
      case "a":
      case "2":
        return GameMode.ADVENTURE;
      case "creative":
      case "c":
      case "1":
        return GameMode.CREATIVE;
    }
    return null;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 2) return CommandHelper.getCompletions(sender);
    if (args.length > 1) return null;

    List<String> allowed = new ArrayList<>();
    for (GameMode gamemode : GameMode.values()) {
      if (!sender.hasPermission(getPermission(gamemode))) continue;
      allowed.addAll(switch (gamemode) {
        case SURVIVAL -> List.of("0", "survival", "s");
        case CREATIVE -> List.of("1", "creative", "c");
        case ADVENTURE -> List.of("2", "adventure", "a");
        case SPECTATOR -> List.of("3", "spectator", "sp");
      });
    }
    return Utils.filterRecommendations(args[0], allowed.toArray(new String[0]));
  }

}
