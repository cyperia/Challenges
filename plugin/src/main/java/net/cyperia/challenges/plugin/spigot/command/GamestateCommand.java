package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.common.config.FileDocument;
import org.jetbrains.annotations.NotNull;

public class GamestateCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("gamestate", ChallengesPermissions.COMMAND_GAMESTATE);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.then(nodes.literal("reset").requires(nodes.permission(ChallengesPermissions.COMMAND_GAMESTATE_RESET))
      .executes(nodes.run((sender, context) -> {
        gamestate().clear();
        Challenges.getInstance().getChallengeManager().resetGamestate();
        Challenges.getInstance().getScoreboardManager().updateAll();
        MessageKey.of("command.gamestate.reset").send(sender, Prefix.CHALLENGES);
      })));
    root.then(nodes.literal("reload").requires(nodes.permission(ChallengesPermissions.COMMAND_GAMESTATE_RELOAD))
      .executes(nodes.run((sender, context) -> {
        Challenges.getInstance().getChallengeManager().loadGamestate(gamestate().readonly());
        Challenges.getInstance().getScoreboardManager().updateAll();
        MessageKey.of("command.gamestate.reloaded").send(sender, Prefix.CHALLENGES);
      })));
  }

  @NotNull
  private static FileDocument gamestate() {
    return Challenges.getInstance().getConfigManager().getGamestateConfig();
  }

}
