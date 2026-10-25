package net.cyperia.challenges.plugin.management.server;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeActionBar;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeScoreboard;
import net.cyperia.challenges.plugin.management.server.scoreboard.PlayerScoreboards;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ScoreboardManager {

  private final List<ChallengeBossBar> bossbars = new CopyOnWriteArrayList<>();
  private volatile ChallengeScoreboard currentScoreboard;
  private volatile ChallengeActionBar currentActionBar;

  public ScoreboardManager() {
    ChallengeAPI.subscribeLoader(LanguageLoader.class, this::updateAll);
    ChallengeAPI.registerScheduler(this);
  }

  public void handleQuit(@NotNull Player player) {
    for (ChallengeBossBar bossbar : bossbars) {
      bossbar.applyHide(player);
    }
    if (currentScoreboard != null) {
      currentScoreboard.applyHide(player);
      Regions.later(Challenges.getInstance(), () -> currentScoreboard.update(), 1);
    }
    PlayerScoreboards.forget(player);
  }

  public void handleJoin(@NotNull Player player) {
    updateAll();
  }

  @ScheduledTask(timerPolicy = TimerPolicy.COUNTDOWN, ticks = 20, async = false)
  @TimerTask(status = {TimerStatus.RUNNING, TimerStatus.PAUSED, TimerStatus.COUNTDOWN}, async = false)
  public void updateAll() {
    for (ChallengeBossBar bossbar : bossbars) {
      bossbar.update();
    }
    if (currentScoreboard != null) {
      currentScoreboard.update();
    }
    if (currentActionBar != null) {
      currentActionBar.send();
    }
  }

  @NotNull
  public List<ChallengeBossBar> getCurrentBossBars() {
    return bossbars;
  }

  public void showBossBar(@NotNull ChallengeBossBar bossbar) {
    if (bossbars.contains(bossbar)) return;
    bossbars.add(bossbar);
    bossbar.update();
  }

  public void hideBossBar(@NotNull ChallengeBossBar bossbar) {
    bossbar.applyHide();
    bossbars.remove(bossbar);
  }

  @Nullable
  public ChallengeScoreboard getCurrentScoreboard() {
    return currentScoreboard;
  }

  public void setCurrentScoreboard(@Nullable ChallengeScoreboard scoreboard) {
    if (currentScoreboard == scoreboard) return;

    // Remove old scoreboard
    if (currentScoreboard != null) {
      Challenges.getInstance().getPlayerManager().forEachAudience(currentScoreboard::applyHide);
    }

    currentScoreboard = scoreboard;

    // Add new scoreboard if available
    if (scoreboard == null) return;
    scoreboard.update();
  }

  @Nullable
  public ChallengeActionBar getCurrentActionBar() {
    return currentActionBar;
  }

  public void setCurrentActionBar(@Nullable ChallengeActionBar actionbar) {
    if (currentActionBar == actionbar) return;

    currentActionBar = actionbar;
    Challenges.getInstance().getChallengeTimer().updateActionbar();
  }

  public void disable() {
    for (ChallengeBossBar bossbar : bossbars.toArray(new ChallengeBossBar[0])) {
      hideBossBar(bossbar);
    }
    setCurrentScoreboard(null);
  }

  public boolean isShown(@NotNull ChallengeBossBar bossbar) {
    return bossbars.contains(bossbar);
  }

  public boolean isShown(@NotNull ChallengeScoreboard scoreboard) {
    return currentScoreboard == scoreboard;
  }

  public boolean isShown(@NotNull ChallengeActionBar actionbar) {
    return currentActionBar == actionbar;
  }

}
