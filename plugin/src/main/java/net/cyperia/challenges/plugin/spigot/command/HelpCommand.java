package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.jetbrains.annotations.NotNull;

/**
 * Answers nothing itself: {@link net.cyperia.challenges.plugin.spigot.listener.HelpListener} adds the
 * plugin info to every help command, also to one another plugin owns.
 */
public class HelpCommand implements ChallengeCommand {

  public static final CommandInfo INFO = CommandInfo.command("help", ChallengesPermissions.COMMAND_HELP)
    .withAliases("hilfe", "h", "info").asGeneric();

  @NotNull
  @Override
  public CommandInfo info() {
    return INFO;
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.executes(nodes.run((sender, context) -> {
    }));
  }

}
