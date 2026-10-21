package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.implementation.setting.PositionSetting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class WorldCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("world", ChallengesPermissions.COMMAND_WORLD);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    for (String name : new String[]{"overworld", "nether", "end"}) {
      root.then(nodes.literal(name).executes(nodes.runAsPlayer((player, context) -> {
        Environment environment = PositionSetting.getWorldEnvironment(name);
        World world = environment != null ? ChallengeAPI.getGameWorld(environment) : null;
        if (world == null) {
          MessageKey.of("command.world.not-loaded").send(player, Prefix.CHALLENGES);
          return;
        }

        Location location = getSpawn(world, player);
        MessageKey.of("command.world.done").send(player, Prefix.CHALLENGES, getWorldName(location));
        player.teleport(location);
      })));
    }
  }

  public Location getSpawn(@NotNull World world, @NotNull Player player) {
    Location location = BlockUtils.getCenteredSpawn(world);
    Location bedSpawnLocation = player.getBedSpawnLocation();
    if (bedSpawnLocation != null && bedSpawnLocation.getWorld() == world) {
      location = bedSpawnLocation;
    } else if (world.getEnvironment() == Environment.THE_END) {
      location = world.getHighestBlockAt(0, 0).getLocation().add(0.5, 1, 0.5);
    }
    return location;
  }

  public String getWorldName(@NotNull Location location) {
    if (location.getWorld() == null) return "?";
    switch (location.getWorld().getEnvironment()) {
      default:
        return "Overworld";
      case NETHER:
        return "Nether";
      case THE_END:
        return "End";
    }
  }


}
