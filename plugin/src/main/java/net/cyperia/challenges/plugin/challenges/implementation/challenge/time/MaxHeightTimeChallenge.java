package net.cyperia.challenges.plugin.challenges.implementation.challenge.time;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class MaxHeightTimeChallenge extends SettingModifier {

  public MaxHeightTimeChallenge() {
    super(3, 20);
    setValueUnit(ValueUnit.MINUTES);
    setUsesGlobalPunish(true);
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      int currentTime = getCurrentTime(player);
      int maxTime = (getValue() * 60);
      bossbar.setTitle(getChallengeMessageKey("bossbar"), player.getLocation().getBlockY(), ChallengeAPI.formatTime(maxTime - currentTime));
      bossbar.setColor(BossBar.Color.GREEN);
      bossbar.setProgress(1 - ((float) currentTime / maxTime));
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @Override
  protected void onValueChange() {
    bossbar.update();
  }

  @EventHandler
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;
    if (event.getFrom().getBlockY() == event.getTo().getBlockY()) return;
    Bukkit.getScheduler().runTask(plugin, () -> bossbar.update(event.getPlayer()));
  }

  @ScheduledTask(ticks = 20)
  public void onSecond() {
    broadcastAudience(this::updateHeightTime);
  }

  private void updateHeightTime(@NotNull Player player) {
    if (ignorePlayer(player)) {
      bossbar.update(player);
      return;
    }

    int time = getCurrentTime(player) + 1;
    if (time > getValue() * 60) {
      ChallengeHelper.punish(player);
    }
    if (time < getValue() * 60) {
      getPlayerData(player).set(String.valueOf(player.getLocation().getBlockY()), time);
    }
    bossbar.update(player);
  }

  private int getCurrentTime(@NotNull Player player) {
    return getPlayerData(player).getInt(String.valueOf(player.getLocation().getBlockY()), 0);
  }

}
