package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingGoal;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import org.bukkit.Raid.RaidStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.raid.RaidFinishEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FinishRaidGoal extends SettingGoal {


  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onRaidFinish(@NotNull RaidFinishEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getRaid().getStatus() != RaidStatus.VICTORY) return;
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED, event::getWinners);
  }

}
