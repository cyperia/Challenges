package net.cyperia.challenges.plugin;

import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.loader.ContentLoader;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public final class ChallengeAPI {

  private ChallengeAPI() {
  }

  public static boolean isStarted() {
    return Challenges.getInstance().getChallengeTimer().isStarted();
  }

  public static boolean isPaused() {
    return Challenges.getInstance().getChallengeTimer().isPaused();
  }

  public static boolean isCountdown() {
    return Challenges.getInstance().getChallengeTimer().isCountdownRunning();
  }

  public static int getCountdownSecondsLeft() {
    return Challenges.getInstance().getChallengeTimer().getCountdown();
  }

  public static float getCountdownProgress() {
    return Challenges.getInstance().getChallengeTimer().getCountdownProgress();
  }

  public static TimerStatus getTimerStatus() {
    return Challenges.getInstance().getChallengeTimer().getStatus();
  }

  public static void pauseTimer() {
    Challenges.getInstance().getChallengeTimer().pause(true);
  }

  public static void pauseTimer(boolean playInGameEffects) {
    Challenges.getInstance().getChallengeTimer().pause(playInGameEffects);
  }

  public static void resumeTimer() {
    Challenges.getInstance().getChallengeTimer().resume();
  }

  public static void resetTimer() {
    Challenges.getInstance().getChallengeTimer().reset();
  }

  public static void endChallenge(@NotNull ChallengeEndCause endCause) {
    Challenges.getInstance().getServerManager().endChallenge(endCause, null);
  }

  public static void endChallenge(@NotNull ChallengeEndCause endCause, Supplier<List<Player>> winnerGetter) {
    Challenges.getInstance().getServerManager().endChallenge(endCause, winnerGetter);
  }


  public static boolean isWorldInUse() {
    return Challenges.getInstance().getWorldManager().isWorldInUse();
  }

  public static boolean isFresh() {
    return Challenges.getInstance().getServerManager().isFresh();
  }

  public static void registerScheduler(@NotNull Object... scheduler) {
    Challenges.getInstance().getScheduler().register(scheduler);
  }

  public static void subscribeLoader(@NotNull Class<? extends ContentLoader> classOfLoader, @NotNull Runnable action) {
    Challenges.getInstance().getLoaderRegistry().subscribe(classOfLoader, action);
  }

  @Nullable
  public static Collection<ItemStack> getCustomDrops(@NotNull Block block, @Nullable ItemStack tool, @Nullable Entity breaker) {
    return Challenges.getInstance().getBlockDropManager().getCustomDrops(block, tool, breaker);
  }

  public static boolean getDropChance(@NotNull Material block) {
    return Challenges.getInstance().getBlockDropManager().getDropChance(block).getAsBoolean();
  }

  public static boolean getItemsDirectIntoInventory() {
    return Challenges.getInstance().getBlockDropManager().isItemsDirectIntoInventory();
  }

  @NotNull
  public static LocalizableMessage formatTime(long seconds) {
    return Challenges.getInstance().getChallengeTimer().getFormattedTimeFor(seconds);
  }

  /**
   * @return all players that aren't ignored by the plugin
   */
  @NotNull
  public static List<Player> getIngamePlayers() {
    return Challenges.getInstance().getPlayerManager().getChallengePlayers();
  }

  public static List<World> getGameWorlds() {
    return Challenges.getInstance().getGameWorldStorage().getGameWorlds();
  }

  public static World getGameWorld(@Nullable Environment environment) {
    return Challenges.getInstance().getGameWorldStorage().getWorld(environment);
  }

  public static boolean isPlayerInGameWorld(@NotNull Environment environment) {
    World world = getGameWorld(environment);
    return world != null && isPlayerInGameWorld(world);
  }

  public static boolean isPlayerInGameWorld(@NotNull World world) {
    for (Player player : world.getPlayers()) {
      if (!AbstractChallenge.ignorePlayer(player)) {
        return true;
      }
    }
    return false;
  }

}
