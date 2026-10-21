package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

/** {@code /gms}, {@code /gmc}, ... for {@code /gamemode <mode> [player]}. */
public class GamemodeShortcutCommand implements ChallengeCommand {

  private final String literal;
  private final GameMode gamemode;

  public GamemodeShortcutCommand(@NotNull String literal, @NotNull GameMode gamemode) {
    this.literal = literal;
    this.gamemode = gamemode;
  }

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command(literal, ChallengesPermissions.COMMAND_GAMEMODE).asGeneric();
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.requires(root.getRequirement().and(nodes.permission(ChallengesPermissions.commandGamemode(gamemode))));
    GamemodeCommand.target(root, nodes, gamemode);
  }

}
