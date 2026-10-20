package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WeatherCommand implements PlayerCommand, Completer {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {

    if (args.length == 0) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "weather <sun/clear/rain/thunder>");
      return;
    }

    World world = player.getWorld();

    switch (args[0].toLowerCase()) {

      case "clear":
      case "sun":
        world.setStorm(false);
        world.setThundering(false);
        MessageKey.of("command.weather.clear").send(player, Prefix.CHALLENGES);
        break;
      case "rain":
        world.setThundering(false);
        world.setStorm(true);
        MessageKey.of("command.weather.rain").send(player, Prefix.CHALLENGES);
        break;
      case "thunder":
        world.setStorm(true);
        world.setThundering(true);
        MessageKey.of("command.weather.thunder").send(player, Prefix.CHALLENGES);
        break;
      default:
        MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "weather <sun/clear/rain/thunder>");
    }

  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length > 1) return new ArrayList<>();
    return Utils.filterRecommendations(args[0], "sun", "clear", "rain", "thunder");
  }

}
