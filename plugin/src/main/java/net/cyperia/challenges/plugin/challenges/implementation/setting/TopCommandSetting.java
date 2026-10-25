package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandProvider;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.spigot.command.ShortcutCommand;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TopCommandSetting extends Setting implements CommandProvider {

  @NotNull
  @Override
  public List<ChallengeCommand> getCommands() {
    return List.of(ShortcutCommand.forPlayer(CommandInfo.command("top", ChallengesPermissions.COMMAND_TOP).withAliases("up"),
      (player, context) -> teleportUp(player)));
  }

  private void teleportUp(@NotNull Player player) {
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
      teleport(player, location);

    } else {
      getChallengeMessageKey("overworld").send(player, Prefix.CHALLENGES);

      if (environment == Environment.NETHER) {
        Location target = playerLocation.clone();
        World overworld = ChallengeAPI.getGameWorld(Environment.NORMAL);
        target.setWorld(overworld);
        target.multiply(8);
        overworld.getChunkAtAsync(target).thenAccept(chunk -> Regions.at(plugin, target, () ->
          teleport(player, overworld.getHighestBlockAt(target).getLocation().add(0.5, 1, 0.5))));
      } else {
        Location location = player.getBedSpawnLocation();
        if (location == null) location = BlockUtils.getCenteredSpawn(ChallengeAPI.getGameWorld(Environment.NORMAL));
        teleport(player, location);
      }

    }
  }

  private void teleport(@NotNull Player player, @NotNull Location location) {
    Regions.entity(plugin, player, () -> {
      player.setFallDistance(0);
      player.teleportAsync(location).thenAccept(success -> SoundSample.TELEPORT.play(player));
    });
  }

}
