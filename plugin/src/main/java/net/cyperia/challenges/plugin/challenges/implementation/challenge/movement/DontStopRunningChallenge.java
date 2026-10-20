package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DontStopRunningChallenge extends SettingModifier {

  private static final int YELLOW = 5, RED = 3;

  private final Map<Player, Integer> playerStandingCount = new HashMap<>();

  public DontStopRunningChallenge() {
    super(3, 30, 10);
    setValueUnit(ValueUnit.SECONDS);
    setUsesGlobalPunish(true);
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      int count = playerStandingCount.getOrDefault(player, 1);
      int timeLeft = getValue() - count + 1;

      if (timeLeft <= RED) bossbar.setColor(BossBar.Color.RED);
      else if (timeLeft <= YELLOW) bossbar.setColor(BossBar.Color.YELLOW);
      else bossbar.setColor(BossBar.Color.GREEN);

      bossbar.setTitle(getChallengeMessageKey("bossbar"), ChallengeAPI.formatTime(timeLeft));
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @ScheduledTask(ticks = 20)
  public void onSecond() {
    removeOfflinePlayers();
    countUpOrKillEveryone();
    bossbar.update();
  }

  private void removeOfflinePlayers() {
    for (Player player : new ArrayList<>(playerStandingCount.keySet())) {
      if (!player.isOnline() || ignorePlayer(player)) playerStandingCount.remove(player);
    }
  }

  private void countUpOrKillEveryone() {

    broadcastFiltered(player -> {
      Integer count = playerStandingCount.getOrDefault(player, 0);
      if (count >= getValue()) {
        getChallengeMessageKey("stopped-moving").broadcast(Prefix.CHALLENGES, player);
        playerStandingCount.remove(player);
        ChallengeHelper.punish(player);
        return;
      }
      playerStandingCount.put(player, count + 1);

    });

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;
    if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getZ() == event.getTo().getZ() && event.getFrom().getY() == event.getTo().getY())
      return;
    playerStandingCount.remove(event.getPlayer());
  }

}
