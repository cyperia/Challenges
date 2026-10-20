package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingGoal;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FirstOneToDieGoal extends SettingGoal {

  private Player winner;

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    if (winner != null)
      winners.add(winner);
  }

  @Override
  protected void onDisable() {
    winner = null;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDeath(@NotNull PlayerDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    winner = event.getEntity();
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
  }

}
