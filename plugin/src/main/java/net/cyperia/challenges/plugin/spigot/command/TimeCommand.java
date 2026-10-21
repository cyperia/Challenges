package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.common.collection.NumberFormatter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

public class TimeCommand implements ChallengeCommand {

  private static final long DAY_LENGTH = 24000;
  private static final Map<String, Long> NAMED_TIMES = Map.of("day", 1000L, "noon", 6000L, "night", 13000L, "midnight", 18000L);

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("time", ChallengesPermissions.COMMAND_TIME).asGeneric();
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    LiteralArgumentBuilder<S> set = nodes.literal("set");
    NAMED_TIMES.forEach((name, time) -> {
      root.then(nodes.literal(name).executes(nodes.runAsPlayer((player, context) -> set(player, time))));
      set.then(nodes.literal(name).executes(nodes.runAsPlayer((player, context) -> set(player, time))));
    });
    root.then(set.then(nodes.argument("ticks", LongArgumentType.longArg(0))
      .executes(nodes.runAsPlayer((player, context) -> set(player, LongArgumentType.getLong(context, "ticks"))))));
    root.then(nodes.literal("add").then(nodes.argument("ticks", LongArgumentType.longArg(0))
      .executes(nodes.runAsPlayer((player, context) -> set(player, player.getWorld().getTime() + LongArgumentType.getLong(context, "ticks"))))));
    root.then(nodes.literal("subtract").then(nodes.argument("ticks", LongArgumentType.longArg(0))
      .executes(nodes.runAsPlayer((player, context) -> set(player, player.getWorld().getTime() - LongArgumentType.getLong(context, "ticks"))))));
    root.then(nodes.literal("query").executes(nodes.runAsPlayer((player, context) -> {
      World world = player.getWorld();
      MessageKey.of("command.time.query").send(player, Prefix.CHALLENGES, NumberFormatter.MIDDLE_NUMBER.format(world.getFullTime()),
        world.getFullTime() / DAY_LENGTH, NumberFormatter.MIDDLE_NUMBER.format(world.getTime()), getNearestTime(world));
    })));
  }

  @NotNull
  public static ShortcutCommand shortcut(@NotNull String name) {
    long time = NAMED_TIMES.get(name);
    return ShortcutCommand.forPlayer(CommandInfo.command(name, ChallengesPermissions.COMMAND_TIME).asGeneric(),
      (player, context) -> set(player, time));
  }

  // World#setTime moves on to the next time it is that time of day, so a negative time or one past the day works too
  private static void set(@NotNull Player player, long time) {
    World world = player.getWorld();
    world.setTime(time);
    long timeOfDay = Math.floorMod(time, DAY_LENGTH);
    NAMED_TIMES.entrySet().stream()
      .filter(entry -> entry.getValue() == timeOfDay)
      .findFirst()
      .ifPresentOrElse(
        entry -> MessageKey.of("command.time.done-exact").send(player, Prefix.CHALLENGES, MessageKey.of("command.time", entry.getKey()), timeOfDay),
        () -> MessageKey.of("command.time.done").send(player, Prefix.CHALLENGES, timeOfDay, getNearestTime(world)));
  }

  @NotNull
  private static String getNearestTime(@NotNull World world) {
    String name = NAMED_TIMES.entrySet().stream()
      .min(Comparator.comparingLong(entry -> Math.abs(world.getTime() - entry.getValue())))
      .map(Entry::getKey)
      .orElse("day");
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

}
