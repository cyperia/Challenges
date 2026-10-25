package net.cyperia.challenges.plugin.management.server;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps an unattended server usable across back-to-back runs: it puts players who join a run that is
 * already going into spectator mode, and resets the server on its own once the run is over or the
 * server has been empty for a while.
 *
 * <p>Both resets go through {@link WorldManager#prepareWorldReset(org.bukkit.command.CommandSender)},
 * so they behave exactly like {@code /reset} — including {@code restart-on-reset}, which has to be
 * enabled for the server to come back up by itself.
 */
public final class AutoResetManager implements Listener {

  private final boolean spectateOnLateJoin;
  private final int afterGoalSeconds;
  private final int afterEmptySeconds;

  /**
   * Everyone who was online at the moment the timer was started — the run's participants. They keep
   * playing normally when they reconnect; anyone else joining while the timer runs missed the start
   * and can only spectate.
   */
  private final Set<UUID> participants = new HashSet<>();
  private static final String PARTICIPANTS_KEY = "timer.participants";

  private static final Set<Integer> COUNTDOWN_ANNOUNCED_AT = Set.of(120, 60, 30, 10, 5, 3, 2, 1);

  private volatile ScheduledTask pendingReset;
  private boolean cancelPendingResetOnJoin;
  private int secondsUntilReset;

  public AutoResetManager() {
    Document config = Challenges.getInstance().getConfigDocument();
    spectateOnLateJoin = config.getBoolean(ConfigManager.Keys.SPECTATE_ON_LATE_JOIN);
    // Explicit -1 defaults: an existing config.yml that predates these keys must stay disabled,
    // not fall through to getInt's 0 and reset the server the instant a run ends.
    afterGoalSeconds = config.getInt(ConfigManager.Keys.AUTO_RESET_AFTER_GOAL, -1);
    afterEmptySeconds = config.getInt(ConfigManager.Keys.AUTO_RESET_AFTER_EMPTY, -1);
    participants.addAll(Challenges.getInstance().getConfigManager().getSessionConfig().getUUIDList(PARTICIPANTS_KEY));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    Player player = event.getPlayer();

    if (isLateJoinSpectator(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      MessageKey.of("server.late-join-spectator").send(player, Prefix.CHALLENGES);
    }

    // Checked after the switch above, so a late joiner who just became a spectator does not call
    // off a countdown they are not going to play out.
    if (cancelPendingResetOnJoin && isPlaying(player)) {
      cancelEmptyCountdown();
    }
  }

  /**
   * Whether this player missed the start of the run that is going and is therefore only watching it.
   *
   * @implNote Answers before the spectator game mode is applied, so a join handler running ahead of
   * this one can already tell the two apart. An unknown list (a session saved before the list was)
   * means everyone is treated as a participant rather than locking a whole run out of its own game.
   */
  public boolean isLateJoinSpectator(@NotNull Player player) {
    return spectateOnLateJoin
      && ChallengeAPI.isStarted()
      && !isParticipant(player.getUniqueId());
  }

  /**
   * Whether this player was online when the timer was last started. Everyone is, before the first
   * start and when the session holds no list; unlike {@link #isLateJoinSpectator} it also holds
   * while the run is paused.
   */
  public boolean isParticipant(@NotNull UUID player) {
    return participants.isEmpty() || participants.contains(player);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(@NotNull PlayerQuitEvent event) {
    // The quitting player is still online at this point, so they are excluded explicitly
    startEmptyCountdownIfNobodyIsPlaying(event.getPlayer());
  }

  /**
   * Reacts to a player starting or stopping to count, whichever way it happened.
   *
   * @implNote Deliberately not a {@link org.bukkit.event.player.PlayerGameModeChangeEvent} listener:
   * a staff member who unvanishes is playing again without ever changing game mode, and watching only
   * game modes let an abandoned round reset with somebody standing in it.
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onIgnoreStatusChange(@NotNull PlayerIgnoreStatusChangeEvent event) {
    if (event.isIgnored()) {
      startEmptyCountdownIfNobodyIsPlaying(event.getPlayer());
    } else if (cancelPendingResetOnJoin) {
      cancelEmptyCountdown();
    }
  }

  /**
   * Starts the empty-server countdown unless somebody other than {@code excluded} is still playing.
   * "Playing" is the plugin's own notion: spectators and creative players are ignored exactly as
   * {@code ignore-players} says, so a spectator left behind does not keep an abandoned run alive.
   */
  private void startEmptyCountdownIfNobodyIsPlaying(@NotNull Player excluded) {
    if (afterEmptySeconds < 0) return;
    if (pendingReset != null) return; // a reset is already coming, e.g. because the goal was reached
    if (ChallengeAPI.isFresh()) return; // nothing was ever played, nothing to reset
    if (Challenges.getInstance().getWorldManager().isShutdownBecauseOfReset()) return;

    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player != excluded && isPlaying(player)) return;
    }

    startEmptyCountdown();
  }

  /**
   * Counts the empty-server reset down out loud, because the players it throws out are the ones who
   * are only watching: nobody is playing, or this countdown would not be running.
   */
  private void startEmptyCountdown() {
    cancelPendingReset();
    cancelPendingResetOnJoin = true;
    secondsUntilReset = afterEmptySeconds;
    pendingReset = Regions.timer(Challenges.getInstance(), _ -> countDownToReset(), 20L, 20L);
  }

  private void countDownToReset() {
    secondsUntilReset--;
    if (secondsUntilReset <= 0) {
      cancelPendingReset();
      Challenges.getInstance().getWorldManager().prepareWorldReset(Bukkit.getConsoleSender());
      return;
    }
    if (COUNTDOWN_ANNOUNCED_AT.contains(secondsUntilReset)) {
      MessageKey.of("server.auto-reset.after-empty").broadcast(Prefix.CHALLENGES, secondsUntilReset);
    }
  }

  /**
   * Calls off a countdown that was announced, and says so. Only the empty-server countdown is ever
   * cancelled, so this never silences the one that follows a reached goal.
   */
  private void cancelEmptyCountdown() {
    if (pendingReset == null) return;
    cancelPendingReset();
    MessageKey.of("server.auto-reset.after-empty-cancelled").broadcast(Prefix.CHALLENGES);
  }

  private static boolean isPlaying(@NotNull Player player) {
    return !AbstractChallenge.ignorePlayer(player);
  }

  /**
   * Called by {@link net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer#resume()}:
   * whoever is online at that moment is playing this run.
   */
  public void handleTimerStart() {
    participants.clear();
    for (Player player : Bukkit.getOnlinePlayers()) {
      participants.add(player.getUniqueId());
    }
    // Written to disk by ChallengeTimer.saveSession, together with the started flag it belongs to.
    Challenges.getInstance().getConfigManager().getSessionConfig()
      .set(PARTICIPANTS_KEY, participants.stream().map(UUID::toString).toList());
  }

  /**
   * Called by {@link ServerManager#endChallenge} once a run is over.
   */
  public void handleChallengeEnd(@NotNull ChallengeEndCause endCause) {
    if (endCause != ChallengeEndCause.GOAL_REACHED) return;
    if (afterGoalSeconds < 0) return;

    scheduleReset(afterGoalSeconds);
    MessageKey.of("server.auto-reset.after-goal").broadcast(Prefix.CHALLENGES, afterGoalSeconds);
  }

  private void scheduleReset(int seconds) {
    cancelPendingReset();
    cancelPendingResetOnJoin = false;
    pendingReset = Regions.later(Challenges.getInstance(),
      () -> Challenges.getInstance().getWorldManager().prepareWorldReset(Bukkit.getConsoleSender()),
      seconds * 20L);
  }

  private void cancelPendingReset() {
    if (pendingReset == null) return;
    pendingReset.cancel();
    pendingReset = null;
  }

}
