package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Warden;
import org.jetbrains.annotations.NotNull;

public class IntervalWardenChallenge extends TimedChallenge {

  // WardenAi.EMERGE_DURATION, the length of the client's emerge animation
  private static final int EMERGE_TICKS = 134;
  private static final int MAX_GROUND_DISTANCE = 8;

  public IntervalWardenChallenge() {
    // the default, 30 seconds, is spelled out in the locale's catalog-name
    super(1, 6, 2, false);
    setValueUnit(ValueUnit.INTERVAL_SECONDS.steps(15, 30, 45, 60, 90, 120));
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();
    broadcastFiltered(IntervalWardenChallenge::emergeAt);
  }

  // Vanilla only plays the emerge for a TRIGGERED spawn, and Bukkit always spawns as COMMAND, so the pose
  // is held by hand with the AI off until the animation is over
  private static void emergeAt(@NotNull Player player) {
    Location location = player.isOnGround() ? player.getLocation() : ground(player.getLocation());
    World world = location.getWorld();
    // not saved with its chunk: one unloaded mid-emerge would come back with its AI off for good
    Warden warden = world.spawn(location, Warden.class, spawned -> {
      spawned.setPersistent(false);
      spawned.setAI(false);
      spawned.setPose(Pose.EMERGING, true);
    });
    world.playSound(location, Sound.ENTITY_WARDEN_AGITATED, 5, 1);
    world.playSound(location, Sound.ENTITY_WARDEN_EMERGE, 5, 1);

    Regions.later(plugin, warden, () -> {
      if (!warden.isValid()) return;
      warden.setPose(Pose.STANDING, false);
      warden.setAI(true);
    }, EMERGE_TICKS);
  }

  // A player in the air still gets the warden out of the floor beneath them
  @NotNull
  private static Location ground(@NotNull Location location) {
    Location ground = location.clone();
    int minHeight = location.getWorld().getMinHeight();
    for (int i = 0; i < MAX_GROUND_DISTANCE && ground.getBlockY() > minHeight; i++) {
      if (!ground.clone().subtract(0, 1, 0).getBlock().isPassable()) {
        ground.setY(ground.getBlockY());
        return ground;
      }
      ground.subtract(0, 1, 0);
    }
    return location;
  }

}
