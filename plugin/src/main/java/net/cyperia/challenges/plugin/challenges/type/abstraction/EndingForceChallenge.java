package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class EndingForceChallenge extends AbstractForceChallenge {

  public EndingForceChallenge() {
  }

  public EndingForceChallenge(int max) {
    super(max);
  }

  public EndingForceChallenge(int min, int max) {
    super(min, max);
  }

  public EndingForceChallenge(int min, int max, int defaultValue) {
    super(min, max, defaultValue);
  }

  @Override
  protected final void handleCountdownEnd() {
    checkAllPlayers();
  }

  private void checkAllPlayers() {
    List<Player> failed = new ArrayList<>();
    for (Player player : ChallengeAPI.getIngamePlayers()) {
      if (isFailing(player)) {
        broadcastFailedMessage(player);
        failed.add(player);
      }
    }
    if (!failed.isEmpty()) {
      killFailedPlayers(failed);
      return;
    }

    broadcastSuccessMessage();
    SoundSample.LEVEL_UP.broadcast();
  }

  private void killFailedPlayers(@NotNull Iterable<? extends Player> failed) {
    if (!ChallengeAPI.isStarted()) return;
    failed.forEach(ChallengeHelper::punish);
  }

  protected abstract boolean isFailing(@NotNull Player player);

  protected abstract void broadcastFailedMessage(@NotNull Player player);

  protected abstract void broadcastSuccessMessage();

}
