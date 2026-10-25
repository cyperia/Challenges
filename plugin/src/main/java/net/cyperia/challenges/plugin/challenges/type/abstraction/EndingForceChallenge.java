package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
    List<Player> players = ChallengeAPI.getIngamePlayers();
    List<Player> failed = new CopyOnWriteArrayList<>();
    if (players.isEmpty()) {
      finishCheck(failed);
      return;
    }

    AtomicInteger remaining = new AtomicInteger(players.size());
    Runnable checked = () -> {
      if (remaining.decrementAndGet() == 0) {
        Regions.global(Challenges.getInstance(), () -> finishCheck(failed));
      }
    };
    for (Player player : players) {
      Regions.entity(Challenges.getInstance(), player, () -> {
        if (isFailing(player)) {
          broadcastFailedMessage(player);
          failed.add(player);
        }
        checked.run();
      }, checked);
    }
  }

  private void finishCheck(@NotNull List<Player> failed) {
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
