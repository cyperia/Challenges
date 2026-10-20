package net.cyperia.challenges.plugin.challenges.type.abstraction;

import lombok.Setter;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar.BossBarInstance;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

@Setter
public abstract class AbstractForceChallenge extends TimedChallenge {

  public static final int WAITING = 0, COUNTDOWN = 1;

  private int state = WAITING;

  public AbstractForceChallenge() {
    super(false);
  }

  public AbstractForceChallenge(int max) {
    super(max, false);
  }

  public AbstractForceChallenge(int min, int max) {
    super(min, max, false);
  }

  public AbstractForceChallenge(int min, int max, int defaultValue) {
    super(min, max, defaultValue, false);
  }

  @Override
  protected void onTimeActivation() {
    switch (state) {
      case WAITING:
        state = COUNTDOWN;
        chooseForcing();
        restartTimer(getForcingTime());
        SoundSample.BASS_ON.broadcast();
        bossbar.update();
        break;
      case COUNTDOWN:
        state = WAITING;
        restartTimer();
        handleCountdownEnd();
        bossbar.update();
        break;
    }
  }

  protected final void endForcing() {
    state = WAITING;
    restartTimer();
    bossbar.update();
  }

  protected abstract void handleCountdownEnd();

  @Override
  protected void handleCountdown() {
    bossbar.update();
  }

  @Override
  protected void onEnable() {
    bossbar.setContent(setupBossbar());
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    if (document.isEmpty()) {
      state = WAITING;
    }
  }

  protected abstract void chooseForcing();

  protected abstract int getForcingTime();

  @NotNull
  protected abstract BiConsumer<BossBarInstance, Player> setupBossbar();

  public final int getState() {
    return state;
  }

}
