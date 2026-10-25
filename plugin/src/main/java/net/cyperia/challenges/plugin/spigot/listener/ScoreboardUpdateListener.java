package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeScoreboard;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ScoreboardUpdateListener implements Listener {

  @EventHandler
  public void onIgnoredChange(PlayerIgnoreStatusChangeEvent event) {

    Regions.nextTick(Challenges.getInstance(), () -> {
      ChallengeScoreboard currentScoreboard = Challenges.getInstance().getScoreboardManager().getCurrentScoreboard();
      if (currentScoreboard != null) {
        currentScoreboard.update();
      }
    });

  }

}
