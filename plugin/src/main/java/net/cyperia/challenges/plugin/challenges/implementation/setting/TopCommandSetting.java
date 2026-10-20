package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TopCommandSetting extends Setting implements PlayerCommand {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) {
    if (!isEnabled()) {
      MessageKey.of("command.feature-disabled").send(player, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.play(player);
      return;
    }

    World world = player.getWorld();
    Environment environment = world.getEnvironment();
    Location playerLocation = player.getLocation();
    if (environment == Environment.NORMAL) {
      getChallengeMessageKey("surface").send(player, Prefix.CHALLENGES);

      Location location = player.getWorld().getHighestBlockAt(playerLocation).getLocation().add(0.5, 1, 0.5);
      location.setYaw(playerLocation.getYaw());
      location.setPitch(playerLocation.getPitch());
      player.setFallDistance(0);
      player.teleport(location);

    } else {
      getChallengeMessageKey("overworld").send(player, Prefix.CHALLENGES);

      if (environment == Environment.NETHER) {
        Location location = playerLocation.clone();
        world = ChallengeAPI.getGameWorld(Environment.NORMAL);
        location.setWorld(world);
        location.multiply(8);
        location = world.getHighestBlockAt(location).getLocation().add(0.5, 1, 0.5);
        player.setFallDistance(0);
        player.teleport(location);
      } else {
        world = ChallengeAPI.getGameWorld(Environment.NORMAL);
        Location location = player.getBedSpawnLocation();
        if (location == null) location = BlockUtils.getCenteredSpawn(world);
        player.setFallDistance(0);
        player.teleport(location);
      }

    }
    SoundSample.TELEPORT.play(player);

  }

}
