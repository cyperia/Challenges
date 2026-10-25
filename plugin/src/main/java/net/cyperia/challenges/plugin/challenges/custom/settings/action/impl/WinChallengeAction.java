package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class WinChallengeAction extends PlayerTargetAction {

  private final List<Player> winner = new CopyOnWriteArrayList<>();

  public WinChallengeAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true));
  }

  @Override
  public void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {
    super.execute(executionData, subActions);
    Regions.global(Challenges.getInstance(), () -> {
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED, () -> winner);
      winner.clear();
    });
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    winner.add(player);
  }

}
