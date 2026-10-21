package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GamemodeCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("gamemode", ChallengesPermissions.COMMAND_GAMEMODE).withAliases("gm").asGeneric();
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    for (GameMode gamemode : GameMode.values()) {
      for (String name : names(gamemode)) {
        root.then(target(nodes.literal(name).requires(nodes.permission(ChallengesPermissions.commandGamemode(gamemode))), nodes, gamemode));
      }
    }
  }

  @NotNull
  static List<String> names(@NotNull GameMode gamemode) {
    return switch (gamemode) {
      case SURVIVAL -> List.of("survival", "s", "0");
      case CREATIVE -> List.of("creative", "c", "1");
      case ADVENTURE -> List.of("adventure", "a", "2");
      case SPECTATOR -> List.of("spectator", "sp", "3");
    };
  }

  @NotNull
  static <S, T extends ArgumentBuilder<S, T>> T target(@NotNull T node, @NotNull CommandNodes<S> nodes, @NotNull GameMode gamemode) {
    return PlayerTargetCommand.withTargets(node, nodes, ChallengesPermissions.COMMAND_GAMEMODE_OTHER, (sender, targets) -> {
      boolean otherPlayers = PlayerTargetCommand.changeAll(sender, targets, player -> {
        MessageKey.of("command.gamemode.done").send(player, Prefix.CHALLENGES, gamemode);
        player.setGameMode(gamemode);
      });
      if (otherPlayers) {
        MessageKey.of("command.gamemode.done-others").send(sender, Prefix.CHALLENGES, gamemode, targets.size());
      }
    });
  }

}
