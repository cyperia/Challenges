package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.implementation.setting.PositionSetting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorldCommand implements PlayerCommand, TabCompleter {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {

    if (args.length < 1) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "world <world>");
      return;
    }

    String worldName = args[0];

    Environment environment = PositionSetting.getWorldEnvironment(worldName);

    World world = environment != null ? ChallengeAPI.getGameWorld(environment) : null;
    if (world == null) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "world <world>");
      return;
    }

    Location location = getSpawn(world, player);

    MessageKey.of("command.world.done").send(player, Prefix.CHALLENGES, getWorldName(location));
    player.teleport(location);
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

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) {
    return Utils.filterRecommendations(args[0], "Overworld", "Nether", "End");
  }

}
