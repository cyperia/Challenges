package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.jetbrains.annotations.NotNull;

public class ResultCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("result", ChallengesPermissions.COMMAND_RESULT);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.executes(nodes.runAsPlayer((player, context) -> {
      if (ChallengeAPI.isPaused()) {
        MessageKey.of("timer.messages.not-started").send(player, Prefix.CHALLENGES);
        SoundSample.BASS_OFF.play(player);
        return;
      }

      IGoal currentGoal = Challenges.getInstance().getChallengeManager().getCurrentGoal();
      if (currentGoal instanceof ForceBattleGoal<?> forceBattleGoal) {
        forceBattleGoal.sendResult(player);
        return;
      }

      MessageKey.of("command.result.none").send(player, Prefix.CHALLENGES);
    }));
  }

}
