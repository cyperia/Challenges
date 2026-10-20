package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;

public class InvertHealthChallenge extends TimedChallenge {

  public InvertHealthChallenge() {
    super(1, 10, 5, false);
    setValueUnit(ValueUnit.INTERVAL_MINUTES.jitter(20));
  }

  public static void invertHealth(Player player) {
    double health = player.getMaxHealth() - player.getHealth();
    if (health <= 0) {
      ChallengeHelper.kill(player);
      return;
    }
    player.setHealth(health);
  }

  @Override
  protected void onTimeActivation() {
    SoundSample.PLOP.broadcast();
    getChallengeMessageKey("inverted").broadcast(Prefix.CHALLENGES);
    broadcastFiltered(InvertHealthChallenge::invertHealth);
    restartTimer();
  }

}
