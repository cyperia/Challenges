package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class CompletableForceChallenge extends AbstractForceChallenge {

  public CompletableForceChallenge() {
  }

  public CompletableForceChallenge(int max) {
    super(max);
  }

  public CompletableForceChallenge(int min, int max) {
    super(min, max);
  }

  public CompletableForceChallenge(int min, int max, int defaultValue) {
    super(min, max, defaultValue);
  }

  @Override
  protected final void handleCountdownEnd() {
    broadcastFailedMessage();
    endForcing();
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_FAILED);
  }

  protected final void completeForcing(@NotNull Player player) {
    if (getState() != COUNTDOWN) return;
    broadcastSuccessMessage(player);
    endForcing();
    SoundSample.LEVEL_UP.broadcast();
  }

  protected abstract void broadcastFailedMessage();

  protected abstract void broadcastSuccessMessage(@NotNull Player player);

}
