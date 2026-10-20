package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingGoal;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LastManStandingGoal extends SettingGoal {

  private Player winner;

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    determineWinner();
    if (winner != null)
      winners.add(winner);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
    if (!isEnabled()) return;
    checkEnd();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLeave(@NotNull PlayerQuitEvent event) {
    if (!isEnabled()) return;
    checkEnd();
  }

  protected void determineWinner() {
    List<Player> living = ChallengeAPI.getIngamePlayers();
    winner = living.size() == 1 ? living.getFirst() : null;
  }

  protected void checkEnd() {
    determineWinner();
    if (winner == null) return;
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
  }

}
