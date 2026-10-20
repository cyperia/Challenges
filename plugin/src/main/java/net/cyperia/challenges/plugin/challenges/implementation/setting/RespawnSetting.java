package net.cyperia.challenges.plugin.challenges.implementation.setting;

import org.bukkit.Particle;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.challenges.plugin.utils.misc.ParticleUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RespawnSetting extends Setting {

  private final Map<Player, Location> locationsBeforeRespawn = new ConcurrentHashMap<>();

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
    Player player = event.getEntity();
    if (isEnabled()) {
      SoundSample.DEATH.play(player);
    } else {
      locationsBeforeRespawn.put(player, player.getLocation());
      player.setGameMode(GameMode.SPECTATOR);

      checkAllPlayersDead();
    }

    ParticleUtils.spawnParticleCylinder(Challenges.getInstance(), player.getLocation(),
      Particle.WITCH, 17, 1, 2);
  }

  public void checkAllPlayersDead() {
    if (ChallengeAPI.isPaused()) return;

    int playersAlive = ChallengeAPI.getIngamePlayers().size();
    if (playersAlive == 0) {
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_FAILED);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onRespawn(@NotNull PlayerRespawnEvent event) {
    if (isEnabled()) return;

    Player player = event.getPlayer();
    Location locationBeforeRespawn = locationsBeforeRespawn.remove(player);
    if (locationBeforeRespawn == null) return;
    if (locationBeforeRespawn.getY() < player.getWorld().getMinHeight())
      return; // If the player dies in the void (y <= 0), we dont want him to spawn there, he would die again
    event.setRespawnLocation(locationBeforeRespawn);
  }

}
