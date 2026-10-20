package net.cyperia.challenges.plugin.management.server;

import org.bukkit.entity.EntityType;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.misc.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public final class ServerManager {

  private final boolean setSpectatorOnWin;
  private final boolean dropItemsOnEnd;
  private final boolean winSounds;

  private boolean isFresh; // indicates if the timer was never started before
  private boolean hasCheated;

  public ServerManager() {
    Document sessionConfig = Challenges.getInstance().getConfigManager().getSessionConfig();
    hasCheated = sessionConfig.getBoolean("cheated");
    isFresh = sessionConfig.getBoolean("fresh", true);

    Document pluginConfig = Challenges.getInstance().getConfigDocument();
    setSpectatorOnWin = pluginConfig.getBoolean(ConfigManager.Keys.SET_SPECTATOR_ON_WIN);
    dropItemsOnEnd = pluginConfig.getBoolean(ConfigManager.Keys.DROP_ITEMS_ON_END);
    winSounds = pluginConfig.getBoolean(ConfigManager.Keys.ENABLED_WIN_SOUNDS);
  }

  public void setNotFresh() {
    isFresh = false;
    Challenges.getInstance().getConfigManager().getSessionConfig().set("fresh", false);
  }

  public void setHasCheated() {
    hasCheated = true;
    Challenges.getInstance().getConfigManager().getSessionConfig().set("cheated", true);
  }

  public boolean isFresh() {
    return isFresh;
  }

  public boolean hasCheated() {
    return hasCheated;
  }

  public void endChallenge(@NotNull ChallengeEndCause endCause, Supplier<List<Player>> winnerGetter) {
    if (!Bukkit.isPrimaryThread()) {
      // calling end challenge logic async results in all kinds of issues as bukkit/paper does not allow certain actions
      // to be performed async (in some versions): PlayerGameModeChangeEvent may only be triggered synchronously
      Logger.debug("End challenge called from async thread, scheduling sync task (Caller: {}", ReflectionUtils.getCallerName(1));
      Bukkit.getScheduler().callSyncMethod(Challenges.getInstance(), () -> {
        endChallenge(endCause, winnerGetter);
        return null;
      });
      return;
    }

    if (ChallengeAPI.isPaused()) {
      Logger.warn("{} tried to end challenge while timer was paused", ReflectionUtils.getCallerName(1));
      return;
    }

    IGoal currentGoal = Challenges.getInstance().getChallengeManager().getCurrentGoal();
    List<Player> winners = new LinkedList<>();
    if (winnerGetter != null) {
      winners = winnerGetter.get();
    } else if (currentGoal != null && endCause.isWinnable()) {
      currentGoal.getWinnersOnEnd(winners);
    }

    if (endCause != ChallengeEndCause.GOAL_REACHED || setSpectatorOnWin) {
      setSpectator();
    }
    if (endCause == ChallengeEndCause.GOAL_REACHED && winSounds && currentGoal != null && currentGoal.getWinSound() != null) {
      currentGoal.getWinSound().broadcast();
    }
    if (dropItemsOnEnd) {
      for (Player player : ChallengeAPI.getIngamePlayers()) {
        if (winners.isEmpty() || winners.contains(player)) continue;
        dropItems(player);
      }
    }

    Challenges.getInstance().getChallengeTimer().pause(false);

    String seed = Bukkit.getWorlds().isEmpty() ? "?" :
      String.valueOf(ChallengeAPI.getGameWorld(Environment.NORMAL).getSeed());
    LocalizableMessage winnersFormat = LocalizableMessage.joinList(winners);
    LocalizableMessage timeFormat = Challenges.getInstance().getChallengeTimer().getFormattedTime();
    endCause.getMessage(!winners.isEmpty()).broadcast(Prefix.CHALLENGES, timeFormat, winnersFormat, seed);

    Challenges.getInstance().getAutoResetManager().handleChallengeEnd(endCause);

  }

  private void setSpectator() {
    for (Player player : ChallengeAPI.getIngamePlayers()) {
      player.setGameMode(GameMode.SPECTATOR);
      SoundSample.BLAST.play(player);

      try {
        player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
      } catch (IllegalArgumentException ex) {
        // We cant spawn fireworks like that in some versions of spigot
      }
    }
  }

  private void dropItems(@NotNull Player player) {
    dropItems(player.getLocation(), player.getInventory().getContents());
    player.getInventory().clear();
  }

  private void dropItems(@NotNull Location location, @Nullable ItemStack[] items) {
    for (ItemStack item : items) {
      if (item == null) continue;
      if (item.getType().isAir()) continue;
      if (location.getWorld() == null) return;
      location.getWorld().dropItem(location, item);
    }
  }

}
