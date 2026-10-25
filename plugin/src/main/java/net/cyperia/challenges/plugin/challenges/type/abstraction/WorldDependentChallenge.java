package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.server.WorldManager.WorldSettings;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public abstract class WorldDependentChallenge extends TimedChallenge {

  private volatile boolean inExtraWorld;
  private volatile BiConsumer<Player, Integer> lastTeleport;
  private final AtomicInteger teleportIndex = new AtomicInteger();

  public WorldDependentChallenge() {
    super(false);
  }

  public WorldDependentChallenge(int max) {
    super(max, false);
  }

  public WorldDependentChallenge(int min, int max) {
    super(min, max, false);
  }

  public WorldDependentChallenge(int min, int max, int defaultValue) {
    super(min, max, defaultValue, false);
  }

  /**
   * Prevents the activation of two world challenges at the same time
   */
  @Override
  protected final void onTimeActivation() {
    if (ChallengeAPI.isWorldInUse()) {
      restartTimer(1);
    } else {
      startWorldChallenge();
    }
  }

  public abstract void startWorldChallenge();

  @Override
  protected boolean getTimerTrigger() {
    return inExtraWorld || !Challenges.getInstance().getWorldManager().isWorldInUse();
  }

  protected void teleportToWorld(boolean allowJoinCatchUp, @NotNull BiConsumer<Player, Integer> action) {
    if (Challenges.getInstance().getWorldManager().isWorldInUse()) return;
    Challenges.getInstance().getWorldManager().setWorldInUse(inExtraWorld = true);
    lastTeleport = allowJoinCatchUp ? action : null;

    teleportIndex.set(0);
    broadcastFiltered(player -> teleport(player, action));
    broadcastIgnored(player -> {
      teleport(player, null);
      teleportSpectator(player);
    });
  }

  protected void teleportBack() {
    if (!Challenges.getInstance().getWorldManager().isWorldInUse()) return;
    Challenges.getInstance().getWorldManager().setWorldInUse(inExtraWorld = false);
    lastTeleport = null;
    teleportIndex.set(0);
  }

  protected void teleportBack(@NotNull Player player) {
    Challenges.getInstance().getWorldManager().restorePlayerData(player);
  }

  private void teleport(@NotNull Player player, @Nullable BiConsumer<Player, Integer> teleport) {
    player.getInventory().clear();
    player.setFoodLevel(20);
    player.setSaturation(20);
    player.setNoDamageTicks(10);
    player.setFallDistance(0);
    player.setHealth(player.getMaxHealth());
    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }
    if (teleport != null) {
      player.setVelocity(new Vector());
      teleport.accept(player, teleportIndex.getAndIncrement());
      SoundSample.TELEPORT.play(player);
    }
  }

  protected void teleportSpectator(@NotNull Player player) {
    player.setGameMode(GameMode.SPECTATOR);
    List<Player> ingamePlayers = ChallengeHelper.getIngamePlayers();
    if (ingamePlayers.isEmpty()) return;
    Player target = ingamePlayers.get(0);
    player.teleportAsync(target.getLocation()).thenAccept(success ->
      Regions.entity(Challenges.getInstance(), player, () -> player.setSpectatorTarget(target)));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    Player player = event.getPlayer();
    // Both paths teleport, which Canvas refuses while the player is still logging in
    Regions.nextTick(Challenges.getInstance(), player, () -> {
      if (isInExtraWorld()) {
        if (lastTeleport == null) return;
        if (Challenges.getInstance().getWorldManager().hasPlayerData(player)) return;
        teleport(player, lastTeleport);
      } else {
        Challenges.getInstance().getWorldManager().restorePlayerData(player);
      }
    });
  }

  @NotNull
  protected final World getExtraWorld() {
    return Challenges.getInstance().getWorldManager().getExtraWorld();
  }

  @NotNull
  protected final WorldSettings getExtraWorldSettings() {
    return Challenges.getInstance().getWorldManager().getSettings();
  }

  public final boolean isInExtraWorld() {
    return inExtraWorld;
  }

}
