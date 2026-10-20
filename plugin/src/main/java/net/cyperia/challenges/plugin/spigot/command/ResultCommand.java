package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResultCommand implements PlayerCommand {
  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
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
  }
}
