package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import com.google.common.collect.Lists;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class WinChallengeAction extends PlayerTargetAction {

  private final List<Player> winner = Lists.newLinkedList();

  public WinChallengeAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true));
  }

  @Override
  public void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {
    super.execute(executionData, subActions);
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED, () -> winner);
    winner.clear();
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    winner.add(player);
  }

}
