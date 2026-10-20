package net.cyperia.challenges.plugin.management.server;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.server.player.GlobalPlayerVisibilityProvider;
import net.cyperia.challenges.plugin.management.server.player.PlayerActionBarPolicy;
import net.cyperia.challenges.plugin.management.server.player.PlayerNameProvider;
import net.cyperia.challenges.plugin.management.server.player.PlayerVisibilityProvider;
import net.cyperia.challenges.plugin.management.server.player.ServerPlayerNameProvider;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * The single place that decides which players the plugin acts on.
 *
 * <p>Three audiences exist, and picking the wrong one is the usual source of bugs:
 * <ul>
 *   <li>{@link #getAudience()} — everyone online. Anything purely presentational: chat, titles,
 *       sounds, bossbars, scoreboards, and the cleanup that undoes such effects.</li>
 *   <li>{@link #getPlayers()} — everyone who is on the server as far as others can tell, i.e. the
 *       audience minus vanished players. Player counts and join/quit handling.</li>
 *   <li>{@link #getChallengePlayers()} — the players a challenge may actually affect: visible and
 *       not ignored by {@code ignore-players}.</li>
 * </ul>
 */
public final class PlayerManager {

  @Getter
  private final boolean ignoreCreativePlayers, ignoreSpectatorPlayers;

  private PlayerVisibilityProvider visibilityProvider;
  @Getter
  private PlayerNameProvider nameProvider = new ServerPlayerNameProvider();
  private PlayerActionBarPolicy actionBarPolicy = player -> false;

  public PlayerManager() {
    this.visibilityProvider = new GlobalPlayerVisibilityProvider();
    Document config = Challenges.getInstance().getConfigDocument();
    this.ignoreCreativePlayers = config.getBoolean(ConfigManager.Keys.IGNORE_CREATIVE_PLAYERS);
    this.ignoreSpectatorPlayers = config.getBoolean(ConfigManager.Keys.IGNORE_SPECTATOR_PLAYERS);
  }

  public void setVisibilityProvider(@NotNull PlayerVisibilityProvider visibilityProvider) {
    Preconditions.checkNotNull(visibilityProvider, "Cannot set PlayerVisibilityProvider to null");
    this.visibilityProvider = visibilityProvider;
  }

  public void setNameProvider(@NotNull PlayerNameProvider nameProvider) {
    Preconditions.checkNotNull(nameProvider, "Cannot set PlayerNameProvider to null");
    this.nameProvider = nameProvider;
  }

  public void setActionBarPolicy(@NotNull PlayerActionBarPolicy actionBarPolicy) {
    Preconditions.checkNotNull(actionBarPolicy, "Cannot set PlayerActionBarPolicy to null");
    this.actionBarPolicy = actionBarPolicy;
  }

  /** Whether this player should receive a reduced-frequency actionbar instead of an animated one */
  public boolean isActionBarReduced(@NotNull Player player) {
    return actionBarPolicy.isReduced(player);
  }

  /**
   * Has to be called by whoever owns the vanish state whenever it changes for a player. A
   * {@link PlayerVisibilityProvider} is only ever asked for the state of the moment, so without this
   * a vanish is invisible to everything that reacts to {@link PlayerIgnoreStatusChangeEvent} — a game
   * mode change is the only half of {@link #isIgnored(Player)} that announces itself.
   */
  public void handleVisibilityChange(@NotNull Player player) {
    Bukkit.getPluginManager().callEvent(new PlayerIgnoreStatusChangeEvent(player, isIgnored(player)));
  }

  @NotNull
  public Collection<? extends Player> getAudience() {
    return Bukkit.getOnlinePlayers();
  }

  @NotNull
  public List<Player> getPlayers() {
    return collect(false);
  }

  @NotNull
  public List<Player> getChallengePlayers() {
    return collect(true);
  }

  /** Whether this player is an active challenge player */
  public boolean isValid(@NotNull Player player) {
    return !isIgnored(player);
  }

  /** Whether this player should be ignored by challenges (spectator or vanished) */
  public boolean isIgnored(@NotNull Player player) {
    return isVanished(player) || isGameModeIgnored(player.getGameMode());
  }

  /**
   * @implNote Judges a game mode on its own, for the one case a {@link Player} cannot answer for:
   * {@link org.bukkit.event.player.PlayerGameModeChangeEvent}, where the player still reports the
   * mode they are leaving.
   */
  public boolean isGameModeIgnored(@NotNull GameMode mode) {
    return (ignoreCreativePlayers && mode == GameMode.CREATIVE)
      || (ignoreSpectatorPlayers && mode == GameMode.SPECTATOR);
  }

  /** Whether this player is visible to others */
  public boolean isVisible(@NotNull Player player) {
    return !visibilityProvider.isPlayerVanished(player);
  }

  /** Whether this player hidden from others */
  public boolean isVanished(@NotNull Player player) {
    return visibilityProvider.isPlayerVanished(player);
  }

  public void forEachAudience(@NotNull Consumer<? super Player> action) {
    getAudience().forEach(action);
  }

  public void forEachChallengePlayer(@NotNull Consumer<? super Player> action) {
    getChallengePlayers().forEach(action);
  }

  public void forEachIgnoredPlayer(@NotNull Consumer<? super Player> action) {
    for (Player player : getAudience()) {
      if (isIgnored(player)) {
        action.accept(player);
      }
    }
  }

  /**
   * @param challengePlayersOnly whether to drop the players ignored by {@code ignore-players} as well
   * @implNote Built fresh, deliberately not cached per tick. Both halves of the filter are a field read
   * or a hash lookup, so a snapshot saves nothing worth having, while a stale one is a real bug: a game
   * mode is applied only <i>after</i> {@link org.bukkit.event.player.PlayerGameModeChangeEvent} has been
   * dispatched, so nothing can invalidate a cache at the moment it takes effect, and a player switched
   * to spectator kept counting as active for the rest of the tick — which is exactly what every
   * "is anybody left" check reads right after switching somebody.
   */
  @NotNull
  private List<Player> collect(boolean challengePlayersOnly) {
    Collection<? extends Player> online = getAudience();
    List<Player> players = new ArrayList<>(online.size());
    for (Player player : online) {
      if (!isVisible(player)) continue;
      if (challengePlayersOnly && isGameModeIgnored(player.getGameMode())) continue;
      players.add(player);
    }
    return Collections.unmodifiableList(players);
  }

}
