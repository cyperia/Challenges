package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.common.collection.NumberFormatter;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class TimeCommand implements PlayerCommand, Completer {

  private final Map<Long, String> names;

  public TimeCommand() {
    names = new HashMap<>();
    names.put(1000L, "Day");
    names.put(6000L, "Noon");
    names.put(13000L, "Night");
    names.put(18000L, "Midnight");
  }

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {

    if (args.length == 0) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "time <set/add/remove/query/day/night/noon/midnight>");
      return;
    }
    World world = player.getWorld();

    switch (args[0].toLowerCase()) {
      case "day":
        player.performCommand("time set day");
        break;
      case "night":
        player.performCommand("time set night");
        break;
      case "noon":
        player.performCommand("time set noon");
        break;
      case "midnight":
        player.performCommand("time set midnight");
        break;
      case "set": {
        if (args.length == 1) {
          MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "time set <ticks/day/night/noon/midnight>");
          break;
        }
        long time = getTime(args[1]);
        if (time < 0) {
          MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "time set <ticks/day/night/noon/midnight>");
          break;
        }
        world.setTime(time);
        if (names.containsKey(time)) {
          String timeName = names.get(time).toLowerCase();
          MessageKey.of("command.time.done-exact").send(player, Prefix.CHALLENGES, MessageKey.of("command.time", timeName), time);
        } else {
          MessageKey.of("command.time.done").send(player, Prefix.CHALLENGES, time, getNearestTime(world));
        }
        break;
      }
      case "add": {
        if (args.length == 1) {
          MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "time add <ticks>");
          break;
        }
        long time = getLongFromString(args[1]);
        if (time < 0) {
          MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "time add <ticks>");
          break;
        }
        player.performCommand("time set " + (world.getTime() + time));
        break;
      }
      case "subtract": {
        if (args.length == 1) {
          MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "time subtract <ticks>");
          break;
        }
        long time = getLongFromString(args[1]);
        if (time < 0) {
          MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "time subtract <ticks>");
          break;
        }
        player.performCommand("time set " + (world.getTime() - time));
        break;
      }
      case "query": {
        MessageKey.of("command.time.query").send(player, Prefix.CHALLENGES, NumberFormatter.MIDDLE_NUMBER.format(world.getFullTime()), world.getFullTime() / 24000, NumberFormatter.MIDDLE_NUMBER.format(world.getTime()), getNearestTime(world));
        break;
      }

    }

  }

  private String getNearestTime(@NotNull World world) {
    return names.entrySet().stream()
      .min(Comparator.comparingLong(entry -> Math.abs(world.getTime() - entry.getKey())))
      .map(Entry::getValue)
      .orElse("Day");
  }

  private long getTime(@NotNull String input) {
    for (Entry<Long, String> entry : names.entrySet()) {
      if (entry.getValue().equalsIgnoreCase(input))
        return entry.getKey();
    }
    return getLongFromString(input);
  }

  private long getLongFromString(@NotNull String input) {
    try {
      return Long.parseLong(input);
    } catch (NumberFormatException ex) {
      return -1;
    }
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length <= 1)
      return Utils.filterRecommendations(args[0], "set", "subtract", "query", "day", "night", "noon", "midnight");
    if (args[0].equalsIgnoreCase("set"))
      return Utils.filterRecommendations(args[1], "day", "night", "noon", "midnight");
    return new ArrayList<>();
  }

}
