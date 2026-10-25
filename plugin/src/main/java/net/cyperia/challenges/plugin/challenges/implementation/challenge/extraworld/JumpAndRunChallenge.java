package net.cyperia.challenges.plugin.challenges.implementation.challenge.extraworld;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.WorldDependentChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.policy.ExtraWorldPolicy;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.bukkit.jumpgeneration.RandomJumpGenerator;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.ParticleUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JumpAndRunChallenge extends WorldDependentChallenge {

  private final List<UUID> lastPlayers = new ArrayList<>();

  private int jumps = 4;
  private int currentJump;
  private int jumpAndRunsDone;

  private Block targetBlock;
  private Block lastBlock;

  private UUID currentPlayer;

  public JumpAndRunChallenge() {
    super(1, 10, 5);
    setValueUnit(ValueUnit.INTERVAL_MINUTES.jitter(30));
    setUsesGlobalPunish(true);
  }

  @Override
  public void onDisable() {
    if (isInExtraWorld())
      exitJumpAndRun();
  }

  @Override
  protected void handleCountdown() {
    switch (getSecondsLeftUntilNextActivation()) {
      case 5:
      case 3:
      case 2:
        getChallengeMessageKey("countdown").broadcast(Prefix.CHALLENGES, getSecondsLeftUntilNextActivation());
        SoundSample.BASS_OFF.broadcast();
        break;
      case 1:
        getChallengeMessageKey("countdown-one").broadcast(Prefix.CHALLENGES);
        SoundSample.BASS_OFF.broadcast();
        break;
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onQuit(@NotNull PlayerQuitEvent event) {
    if (currentPlayer == null) return;
    if (!currentPlayer.equals(event.getPlayer().getUniqueId())) return;
    exitJumpAndRun();
  }

  @Override
  public void startWorldChallenge() {
    startJumpAndRun();
  }

  protected void startJumpAndRun() {
    Player next = getNextPlayer();
    if (next == null) { // everybody online is a spectator or vanished
      restartTimer(1);
      return;
    }

    currentJump = 0;
    currentPlayer = next.getUniqueId();
    lastPlayers.add(currentPlayer);

    actionbar.setContent(_ -> getChallengeMessageKey("actionbar").withArgs(currentJump, jumps));
    actionbar.show();

    buildNextJump();
    teleportToWorld(true, (player, index) -> {
      player.setGameMode(player.getUniqueId().equals(currentPlayer) ? GameMode.SURVIVAL : GameMode.SPECTATOR);
      player.teleportAsync(new Location(getExtraWorld(), 0.5, 101, 0.5));
    });
  }

  protected void buildNextJump() {
    Block previous = lastBlock;
    Block start = targetBlock != null ? targetBlock : getExtraWorld().getBlockAt(0, 100, 0);
    lastBlock = start;

    Material type = getRandomBlockType();
    Regions.at(plugin, start.getLocation(), () -> {
      if (previous != null) previous.setType(Material.AIR);
      start.setType(Material.GOLD_BLOCK);
      targetBlock = new RandomJumpGenerator().next(globalRandom, start, type == Material.CYAN_TERRACOTTA || type == Material.EMERALD_BLOCK, type != Material.COBBLESTONE_WALL && type != Material.SPRUCE_FENCE);
      targetBlock.setType(type);
    });
  }

  @NotNull
  protected Material getRandomBlockType() {
    if (currentJump == jumps - 1) return Material.EMERALD_BLOCK;

    Material[] materials = {
      Material.CYAN_TERRACOTTA,
      Material.CYAN_TERRACOTTA,
      Material.CYAN_TERRACOTTA,
      Material.CYAN_TERRACOTTA,
      Material.END_ROD,
      Material.COBBLESTONE_WALL,
      Material.SPRUCE_FENCE
    };
    return globalRandom.choose(materials);
  }

  @Nullable
  protected Player getNextPlayer() {
    List<Player> candidates = new ArrayList<>(ChallengeHelper.getIngamePlayers());
    candidates.removeIf(player -> lastPlayers.contains(player.getUniqueId()));

    if (candidates.isEmpty()) { // everybody had their turn, start over
      lastPlayers.clear();
      candidates = ChallengeHelper.getIngamePlayers();
    }
    return candidates.isEmpty() ? null : globalRandom.choose(candidates);
  }

  protected void finishJumpAndRun(@NotNull Player player) {
    jumps++;
    jumpAndRunsDone++;

    getChallengeMessageKey("finished").broadcast(Prefix.CHALLENGES, player);
    exitJumpAndRun();
    SoundSample.KLING.broadcast();
  }

  protected void exitJumpAndRun() {
    currentPlayer = null;
    actionbar.hide();
    teleportBack();
    breakJumpAndRun();
    restartTimer();
  }

  protected void breakJumpAndRun() {
    Block last = lastBlock;
    Block target = targetBlock;
    lastBlock = null;
    targetBlock = null;

    if (last != null) Regions.at(plugin, last.getLocation(), () -> last.setType(Material.AIR));
    if (target != null) Regions.at(plugin, target.getLocation(), () -> target.setType(Material.AIR));
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);
    document.set("jumps", jumps);
    document.set("jumpsDone", jumpAndRunsDone);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    jumps = document.getInt("jumps", jumps);
    jumpAndRunsDone = document.getInt("jumpsDone", jumpAndRunsDone);
  }

  @ScheduledTask(ticks = 20, async = false, timerPolicy = TimerPolicy.ALWAYS, worldPolicy = ExtraWorldPolicy.USED)
  public void spawnParticles() {
    Block target = targetBlock;
    if (target == null) return;
    Location center = target.getLocation().add(0.5, 1.05, 0.5);
    Regions.at(plugin, center, () -> ParticleUtils.spawnParticleCircle(center,
      Particle.INSTANT_EFFECT, Color.WHITE, 13, 0.35));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (!event.getPlayer().getUniqueId().equals(currentPlayer)) return;
    if (!isInExtraWorld()) return;
    if (targetBlock == null) return;
    if (event.getTo() == null) return;

    if (BlockUtils.isSameBlockLocation(event.getTo(), targetBlock.getLocation().add(0, 1, 0))) {
      if (++currentJump >= jumps) {
        finishJumpAndRun(event.getPlayer()); // is currentPlayer
      } else {
        SoundSample.PLOP.broadcast();
        actionbar.send();
        buildNextJump();
      }
    } else if (event.getTo().getBlockY() < targetBlock.getY() - 2) {
      exitJumpAndRun();
      ChallengeHelper.punish(event.getPlayer(), 1);
    }

  }

}
