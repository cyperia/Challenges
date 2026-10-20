package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class TrafficLightChallenge extends TimedChallenge {

  private static final int GREEN = 0, YELLOW = 1, RED = 2;

  private int state;

  public TrafficLightChallenge() {
    super(1, 10, 5);
    setValueUnit(ValueUnit.INTERVAL_MINUTES.jitter(20));
    setUsesGlobalPunish(true);
  }

  @Override
  public void onEnable() {
    bossbar.setContent((bossbar, _) -> {
      switch (state) {
        case GREEN -> {
          bossbar.setColor(BossBar.Color.GREEN);
          bossbar.setTitle(getChallengeMessageKey("bossbar.green"));
        }
        case YELLOW -> {
          bossbar.setColor(BossBar.Color.YELLOW);
          bossbar.setTitle(getChallengeMessageKey("bossbar.yellow"));
        }
        case RED -> {
          bossbar.setColor(BossBar.Color.RED);
          bossbar.setTitle(getChallengeMessageKey("bossbar.red"));
        }
      }
    });
    bossbar.show();
  }

  @Override
  public void onDisable() {
    bossbar.hide();
  }

  @Override
  protected void onTimeActivation() {
    switch (state) {
      case GREEN:
        state = YELLOW;
        restartTimer(2);
        SoundSample.BASS_OFF.broadcast();
        bossbar.update();
        break;
      case YELLOW:
        state = RED;
        restartTimer(3);
        SoundSample.BASS_OFF.broadcast();
        bossbar.update();
        break;
      case RED:
        state = GREEN;
        restartTimer();
        SoundSample.BASS_ON.broadcast();
        bossbar.update();
        break;
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (state != RED) return;
    if (event.getTo() == null) return;
    if (BlockUtils.isSameLocation(event.getFrom(), event.getTo())) return;

    state = GREEN;
    bossbar.update();
    restartTimer();

    Player player = event.getPlayer();
    getChallengeMessageKey("fail").broadcast(Prefix.CHALLENGES, player);
    ChallengeHelper.punish(player);
  }

}
