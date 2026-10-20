package net.cyperia.challenges.plugin.management.scheduler.timer;

import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.scheduler.policy.ExtraWorldPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.PlayerCountPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeActionBar;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.FileDocument;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ChallengeTimer {

  private final Map<Locale, TimerFormat> compiledFormats = new ConcurrentHashMap<>();

  private final boolean specificStartSounds, defaultStartSound;
  private final boolean gradientEnabled;
  private final boolean holdWhileEmpty;
  private final int gradientDuration;
  private final GradientDirection gradientDirection;
  private TimerActionBarUpdateScheduler updateActionbarScheduler;
  private Object pausedParticleScheduler;

  @Getter
  private boolean playPausedParticled;
  @Getter
  private long time = 0;
  @Getter
  private int countdown;
  @Getter
  private int initialCountdown;
  @Getter
  private boolean countdownRunning = false;
  @Getter
  private boolean countingUp = true;
  @Getter
  private boolean paused = true;
  @Getter
  private boolean hidden = false;

  public ChallengeTimer() {
    Document pluginConfig = Challenges.getInstance().getConfigDocument();
    specificStartSounds = pluginConfig.getBoolean(ConfigManager.Keys.ENABLE_SPECIFIC_START_SOUNDS);
    defaultStartSound = pluginConfig.getBoolean(ConfigManager.Keys.ENABLE_DEFAULT_START_SOUNDS);
    gradientEnabled = pluginConfig.getBoolean(ConfigManager.Keys.TIMER_GRADIENT_ENABLED);
    gradientDuration = pluginConfig.getInt(ConfigManager.Keys.TIMER_GRADIENT_DURATION);
    gradientDirection = pluginConfig.getEnum(ConfigManager.Keys.TIMER_GRADIENT_DIRECTION, GradientDirection.LTR);
    playPausedParticled = pluginConfig.getBoolean(ConfigManager.Keys.PAUSED_PARTICLES);
    holdWhileEmpty = pluginConfig.getBoolean(ConfigManager.Keys.PAUSE_ON_LAST_LEAVE);

    if (gradientEnabled) {
      setUpdateActionbarScheduler(new GradientActionBarUpdateScheduler());
    } else {
      setUpdateActionbarScheduler(new DefaultActionBarUpdateScheduler());
    }

    setPlayPausedParticleScheduler(new PausedParticleScheduler());

    Challenges.getInstance().getScheduler().register(this);
    Challenges.getInstance().getLoaderRegistry().subscribe(LanguageLoader.class, this::recompileFormats);
    Challenges.getInstance().getLoaderRegistry().subscribe(LanguageLoader.class, this::updateActionbar);
  }

  public void setUpdateActionbarScheduler(@NotNull TimerActionBarUpdateScheduler updateActionbarScheduler) {
    if (this.updateActionbarScheduler != null) {
      Challenges.getInstance().getScheduler().unregister(this.updateActionbarScheduler);
    }

    this.updateActionbarScheduler = updateActionbarScheduler;
    Challenges.getInstance().getScheduler().register(updateActionbarScheduler);
  }

  public void setPlayPausedParticled(boolean play) {
    if (this.playPausedParticled == play) return;
    setPlayPausedParticleScheduler(pausedParticleScheduler);
    this.playPausedParticled = play;
  }

  public void setPlayPausedParticleScheduler(@Nullable Object pausedParticleScheduler) {
    if (this.pausedParticleScheduler != null) {
      Challenges.getInstance().getScheduler().unregister(this.pausedParticleScheduler);
    }

    this.pausedParticleScheduler = pausedParticleScheduler;
    if (pausedParticleScheduler != null && playPausedParticled) {
      Challenges.getInstance().getScheduler().register(pausedParticleScheduler);
    }
  }

  public void recompileFormats() {
    compiledFormats.clear();
    Locale language = Challenges.getInstance().getLoaderRegistry().findLoaderByClassOrThrow(LanguageLoader.class).getConfigLanguage();
    getOrCompileFormat(language);
  }

  public void enable() {
    boolean fresh = Challenges.getInstance().getServerManager().isFresh();
    for (World world : ChallengeAPI.getGameWorlds()) {
      applyPauseRules(world);
      // Only the overworld has a clock since 26.1, setFullTime throws for the nether and the end.
      if (fresh && world.getEnvironment() == World.Environment.NORMAL) world.setFullTime(0);
      // A pregenerated world brings the weather it was saved with
      if (fresh) {
        world.setStorm(false);
        world.setThundering(false);
      }
    }
  }

  // A paused world stands still, so a server waiting in a pool neither turns to night nor starts raining
  public void applyPauseRules(@NotNull World world) {
    // overhaul post-1.21: DO_DAYLIGHT_CYCLE -> ADVANCE_TIME
    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !paused);
    world.setGameRule(GameRule.DO_WEATHER_CYCLE, !paused);
  }

  private void updatePauseRules() {
    ChallengeAPI.getGameWorlds().forEach(this::applyPauseRules);
  }

  @NotNull
  private TimerFormat getOrCompileFormat(@NotNull Locale locale) {
    return compiledFormats.computeIfAbsent(locale, forLocale -> new TimerFormat(MessageKey.empty("timer.time.format"), forLocale));
  }

  @ScheduledTask(ticks = 20, async = false, timerPolicy = TimerPolicy.COUNTDOWN, playerPolicy = PlayerCountPolicy.SOMEONE)
  public void decrementCountdownSecond() {
    countdown--;

    if (countdown <= 0) {
      countdown = 0;
      countdownRunning = false;
      resume();
      return;
    }

    if (countdown % 60 == 0
      || countdown <= 30 && countdown % 10 == 0
      || countdown == 5 || countdown <= 3) {
      SoundSample.BASS_ON.broadcast();
      MessageKey.of("timer.messages.countdown.notification").broadcast(Prefix.TIMER, ArgumentFormat.TIME.apply(countdown));
    }
  }

  @ScheduledTask(ticks = 20, async = false, timerPolicy = TimerPolicy.STARTED, playerPolicy = PlayerCountPolicy.ALWAYS)
  public void incrementTimerSecond() {
    // The run stays started while nobody is here, so the players return to it without a /start.
    if (holdWhileEmpty && Challenges.getInstance().getPlayerManager().getPlayers().isEmpty()) return;
    if (countingUp) time++;
    else time--;

    if (time <= 0) {
      time = 0;
      if (!countingUp) handleHitZero();
      countingUp = true;
    }
  }

  public void updateActionbar() {
    updateActionbarScheduler.updateActionbar();
  }

  private void handleHitZero() {
    ChallengeAPI.endChallenge(ChallengeEndCause.TIMER_HIT_ZERO);
  }

  public void resume() {
    if (!paused) return;
    paused = false;
    countdownRunning = false;

    updateActionbar();
    updatePauseRules();

    MessageKey.of("timer.messages.started").broadcast(Prefix.TIMER);
    Challenges.getInstance().getScheduler().fireTimerStatusChange();
    Challenges.getInstance().getTitleManager().sendTimerStatusTitle(MessageKey.of("title.timer-started"));
    Challenges.getInstance().getServerManager().setNotFresh();
    Challenges.getInstance().getAutoResetManager().handleTimerStart();

    // Spectators are explicitly included: whoever died in the previous run is a spectator now and has
    // to be able to play again. Creative is exempt because it is how the server is set up, not how a
    // run is played, and vanished players are not part of the run at all.
    for (Player player : Challenges.getInstance().getPlayerManager().getPlayers()) {
      if (player.getGameMode() == GameMode.CREATIVE) continue;
      player.setGameMode(GameMode.SURVIVAL);
    }

    IGoal currentGoal = Challenges.getInstance().getChallengeManager().getCurrentGoal();
    if (currentGoal != null && specificStartSounds) {
      currentGoal.getStartSound().broadcast();
    } else if (defaultStartSound) {
      SoundSample.DRAGON_BREATH.broadcast();
    }

  }

  public void pause(boolean playInGameEffects) {
    if (paused) return;
    paused = true;

    updateActionbar();
    updatePauseRules();

    Challenges.getInstance().getScheduler().fireTimerStatusChange();
    if (playInGameEffects) {
      Challenges.getInstance().getTitleManager().sendTimerStatusTitle(MessageKey.of("title.timer-paused"));
      MessageKey.of("timer.messages.paused").broadcast(Prefix.TIMER);
      SoundSample.BASS_OFF.broadcast();
    }
  }

  public void toggle() {
    if (isStarted()) {
      pause(true);
    } else {
      resume();
    }
  }

  public void reset() {
    if (!countingUp) pause(true);
    time = 0;
    countingUp = true;
    updateActionbar();
  }

  @NotNull
  private MessageKey getCurrentActionbarMessage() {
    // TODO save references?
    if (countdownRunning) return MessageKey.of("timer.bar.countdown");
    if (paused) return MessageKey.of("timer.bar.paused");
    if (countingUp) return MessageKey.of("timer.bar.up");
    return MessageKey.of("timer.bar.down");
  }

  public synchronized void loadSession() {
    FileDocument config = Challenges.getInstance().getConfigManager().getSessionConfig();
    time = config.getInt("timer.seconds");
    countingUp = config.getBoolean("timer.countingUp", true);
    hidden = config.getBoolean("timer.hidden", false);
    countdown = config.getInt("timer.countdown.seconds");
    initialCountdown = config.getInt("timer.countdown.initial");
    paused = !config.getBoolean("timer.started", false);
  }

  public synchronized void saveSession(boolean async) {
    FileDocument config = Challenges.getInstance().getConfigManager().getSessionConfig();
    config.set("timer.seconds", time);
    config.set("timer.countingUp", countingUp);
    config.set("timer.hidden", hidden);
    config.set("timer.countdown.seconds", countdown);
    config.set("timer.countdown.initial", initialCountdown);
    config.set("timer.started", !paused);
    config.save(async);
  }

  public void addSeconds(int amount) {
    setSeconds(time + amount);
  }

  public void setSeconds(long seconds) {
    this.time = Math.max(seconds, 0);
    updateActionbar();
  }

  public void addCountdownSeconds(int amount) {
    setCountdown(countdown + amount);
  }

  public void setCountdown(int seconds) {
    this.countdown = Math.max(seconds, 0);
    this.initialCountdown = this.countdown;
    updateActionbar();
  }

  public void resumeCountdown() {
    if (!paused || countdownRunning) return;
    countdownRunning = true;
    updateActionbar();

    Challenges.getInstance().getScheduler().fireTimerStatusChange();

    MessageKey.of("timer.messages.countdown.started").broadcast(Prefix.TIMER, ArgumentFormat.TIME.apply(countdown));
    SoundSample.PLING.broadcast();
  }

  public void pauseCountdown() {
    if (!countdownRunning) return;
    countdownRunning = false;

    updateActionbar();

    Challenges.getInstance().getScheduler().fireTimerStatusChange();

    MessageKey.of("timer.messages.countdown.paused").broadcast(Prefix.TIMER);
    SoundSample.BASS_OFF.broadcast();
  }

  public void toggleCountdown() {
    if (countdownRunning) {
      pauseCountdown();
    } else {
      resumeCountdown();
    }
  }

  public void setHidden(boolean hide) {
    if (this.hidden == hide) return;

    this.hidden = hide;
    updateActionbar();
    MessageKey.of("timer.messages." + (hide ? "hidden" : "shown")).broadcast(Prefix.TIMER);
    SoundSample.BASS_ON.broadcast();
  }

  @NotNull
  public String getFormattedTime(@NotNull Locale locale) {
    return getOrCompileFormat(locale).format(time);
  }

  @NotNull
  public LocalizableMessage getFormattedTime() {
    return getFormattedTimeFor(time);
  }

  @NotNull
  public LocalizableMessage getFormattedDisplayTime() {
    return getFormattedTimeFor(paused ? countdown : time);
  }

  @NotNull
  public LocalizableMessage getFormattedTimeFor(long seconds) {
    return LocalizableMessage.from(locale -> getOrCompileFormat(locale).format(seconds));
  }

  @NotNull
  public TimerStatus getStatus() {
    return countdownRunning ? TimerStatus.COUNTDOWN : paused ? TimerStatus.PAUSED : TimerStatus.RUNNING;
  }

  public boolean isStarted() {
    return !paused;
  }

  public void setCountingUp(boolean countingUp) {
    if (this.countingUp == countingUp) return;

    this.countingUp = countingUp;
    updateActionbar();
    MessageKey.of("timer.messages.counting-" + (countingUp ? "up" : "down")).broadcast(Prefix.TIMER); // TODO
    SoundSample.BASS_ON.broadcast();
  }

  public float getCountdownProgress() {
    if (!countdownRunning || countdown > initialCountdown - 1) return 0;
    if (initialCountdown <= 0) return 0;
    return 1f - (float) countdown / initialCountdown;
  }

  public static class PausedParticleScheduler {

    @ScheduledTask(ticks = 20, timerPolicy = TimerPolicy.PAUSED)
    public void playPausedParticles() {
      for (Player player : ChallengeAPI.getIngamePlayers()) {
        Location location = player.getLocation();
        if (location.getWorld() == null) continue;
        location.getWorld().playEffect(location, Effect.ENDER_SIGNAL, 1);
      }
    }

  }

  public abstract class TimerActionBarUpdateScheduler {

    /** How often a low-network player gets a static actionbar instead of an animated one. */
    static final int REDUCED_INTERVAL_TICKS = 20;

    protected boolean sentEmpty;
    private final int ticksPerUpdate;
    private long tick;

    protected TimerActionBarUpdateScheduler(int ticksPerUpdate) {
      this.ticksPerUpdate = ticksPerUpdate;
    }

    public void updateActionbar() {
      if (shouldNotSendActionbarOrSendEmpty()) return;
      updateTimerActionbar();
    }

    public abstract void updateTimerActionbar();

    protected long getTick() {
      return tick;
    }

    /** Pure: whether this tick should send an actionbar update to a player under the given reduced state. */
    static boolean isDue(boolean reduced, long tick) {
      return !reduced || tick % REDUCED_INTERVAL_TICKS == 0;
    }

    protected boolean shouldNotSendActionbarOrSendEmpty() {
      tick += ticksPerUpdate;

      if (sentEmpty && hidden) return true;
      if (sentEmpty) sentEmpty = false;

      ChallengeActionBar currentActionBar = Challenges.getInstance().getScoreboardManager().getCurrentActionBar();
      if (currentActionBar == null && !hidden) {
        return false;
      }

      if (currentActionBar != null) {
        long currentTick = tick;
        Challenges.getInstance().getPlayerManager().forEachAudience(player -> {
          boolean reduced = Challenges.getInstance().getPlayerManager().isActionBarReduced(player);
          if (isDue(reduced, currentTick)) {
            currentActionBar.send(player);
          }
        });
      } else {
        sentEmpty = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
          player.sendActionBar(Component.empty());
        }
      }

      return true;
    }

  }

  public class DefaultActionBarUpdateScheduler extends TimerActionBarUpdateScheduler {

    public DefaultActionBarUpdateScheduler() {
      super(20);
    }

    @Override
    @ScheduledTask(ticks = 20, timerPolicy = TimerPolicy.ALWAYS, playerPolicy = PlayerCountPolicy.ALWAYS, worldPolicy = ExtraWorldPolicy.ALWAYS)
    public void updateTimerActionbar() {
      if (shouldNotSendActionbarOrSendEmpty()) return;
      getCurrentActionbarMessage().broadcastActionBar(getFormattedDisplayTime(), 0); // default phase to 0 if present
    }

  }

  public class GradientActionBarUpdateScheduler extends TimerActionBarUpdateScheduler {

    private final int maxGradientTickProgress = gradientDuration * 2;
    private int currentGradientTickProgress;

    public GradientActionBarUpdateScheduler() {
      super(1);
    }

    @Override
    @ScheduledTask(ticks = 1, timerPolicy = TimerPolicy.ALWAYS, playerPolicy = PlayerCountPolicy.ALWAYS, worldPolicy = ExtraWorldPolicy.ALWAYS)
    public void updateTimerActionbar() {
      if (shouldNotSendActionbarOrSendEmpty()) return;

      currentGradientTickProgress = gradientDirection.incrementProgress(currentGradientTickProgress, maxGradientTickProgress);

      float phase = (float) currentGradientTickProgress / gradientDuration - 1; // [-1; 1]
      MessageKey message = getCurrentActionbarMessage();
      LocalizableMessage time = getFormattedDisplayTime();
      long currentTick = getTick();

      List<Player> full = new ArrayList<>();
      List<Player> reduced = new ArrayList<>();
      for (Player player : Challenges.getInstance().getPlayerManager().getAudience()) {
        (Challenges.getInstance().getPlayerManager().isActionBarReduced(player) ? reduced : full).add(player);
      }

      message.broadcastActionBar(full, time, phase);
      if (isDue(true, currentTick)) {
        message.broadcastActionBar(reduced, time, 0);
      }
    }

  }

  public enum GradientDirection {

    LTR {
      @Override
      public int incrementProgress(int currentProgress, int maxProgress) {
        if (currentProgress <= 0) {
          return maxProgress;
        } else {
          return currentProgress - 1;
        }
      }

    },
    RTL {
      @Override
      public int incrementProgress(int currentProgress, int maxProgress) {
        if (currentProgress >= maxProgress) {
          return 0;
        } else {
          return currentProgress + 1;
        }
      }
    };

    public abstract int incrementProgress(int currentProgress, int maxProgress);

  }

}
