package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.server.BorderManager;
import net.cyperia.challenges.plugin.management.server.BorderManager.Containment;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

// Replaces the vanilla command: the border players see is the one BorderManager draws, so a vanilla
// border set next to it would be a second one nobody can see
public class WorldborderCommand implements ChallengeCommand {

  // keeps the slide, counted in milliseconds and ticks, far from overflowing
  private static final long MAX_SECONDS = Integer.MAX_VALUE / 20;

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("worldborder", ChallengesPermissions.COMMAND_WORLDBORDER);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.then(nodes.literal("set").then(nodes.argument("size", DoubleArgumentType.doubleArg(BorderManager.MIN_SIZE, BorderManager.MAX_SIZE))
      .executes(nodes.run((sender, context) -> set(sender, DoubleArgumentType.getDouble(context, "size"), 0)))
      .then(nodes.argument("seconds", LongArgumentType.longArg(0, MAX_SECONDS))
        .executes(nodes.run((sender, context) -> set(sender, DoubleArgumentType.getDouble(context, "size"), LongArgumentType.getLong(context, "seconds")))))));
    root.then(nodes.literal("add").then(nodes.argument("size", DoubleArgumentType.doubleArg(-BorderManager.MAX_SIZE, BorderManager.MAX_SIZE))
      .executes(nodes.run((sender, context) -> add(sender, DoubleArgumentType.getDouble(context, "size"), 0)))
      .then(nodes.argument("seconds", LongArgumentType.longArg(0, MAX_SECONDS))
        .executes(nodes.run((sender, context) -> add(sender, DoubleArgumentType.getDouble(context, "size"), LongArgumentType.getLong(context, "seconds")))))));
    root.then(nodes.literal("center")
      .executes(nodes.runAsPlayer((player, context) -> center(player, player.getLocation())))
      .then(nodes.argument("x", DoubleArgumentType.doubleArg()).then(nodes.argument("z", DoubleArgumentType.doubleArg())
        .executes(nodes.run((sender, context) -> center(sender,
          new Location(world(sender), DoubleArgumentType.getDouble(context, "x"), 0, DoubleArgumentType.getDouble(context, "z"))))))));
    root.then(nodes.literal("get").executes(nodes.run((sender, context) -> get(sender))));
    root.then(nodes.literal("damage")
      .then(nodes.literal("buffer").then(nodes.argument("blocks", DoubleArgumentType.doubleArg(0))
        .executes(nodes.run((sender, context) -> {
          double buffer = DoubleArgumentType.getDouble(context, "blocks");
          border().setDamage(buffer, border().getDamagePerBlock());
          MessageKey.of("command.worldborder.damage-buffer").send(sender, Prefix.CHALLENGES, format(buffer));
        }))))
      .then(nodes.literal("amount").then(nodes.argument("damage", DoubleArgumentType.doubleArg(0))
        .executes(nodes.run((sender, context) -> {
          double amount = DoubleArgumentType.getDouble(context, "damage");
          border().setDamage(border().getDamageBuffer(), amount);
          MessageKey.of("command.worldborder.damage-amount").send(sender, Prefix.CHALLENGES, format(amount));
        })))));
    LiteralArgumentBuilder<S> mode = nodes.literal("mode");
    for (Containment containment : Containment.values()) {
      String name = containment.name().toLowerCase(Locale.ROOT);
      mode.then(nodes.literal(name).executes(nodes.run((sender, context) -> {
        border().setContainment(containment);
        MessageKey.of("command.worldborder.mode." + name).send(sender, Prefix.CHALLENGES);
      })));
    }
    root.then(mode);
    root.then(nodes.literal("reset").executes(nodes.run((sender, context) -> {
      Regions.global(Challenges.getInstance(), () -> border().reset());
      MessageKey.of("command.worldborder.reset").send(sender, Prefix.CHALLENGES);
    })));
  }

  private static void set(@NotNull CommandSender sender, double size, long seconds) {
    Regions.global(Challenges.getInstance(), () -> border().setSize(size, seconds));
    sendSize(sender, size, seconds);
  }

  private static void add(@NotNull CommandSender sender, double delta, long seconds) {
    double size = Math.clamp(border().getSize(world(sender)) + delta, BorderManager.MIN_SIZE, BorderManager.MAX_SIZE);
    set(sender, size, seconds);
  }

  private static void sendSize(@NotNull CommandSender sender, double size, long seconds) {
    if (seconds > 0) {
      MessageKey.of("command.worldborder.set-over").send(sender, Prefix.CHALLENGES, format(size), seconds);
    } else {
      MessageKey.of("command.worldborder.set").send(sender, Prefix.CHALLENGES, format(size));
    }
  }

  private static void center(@NotNull CommandSender sender, @NotNull Location center) {
    Regions.global(Challenges.getInstance(), () -> border().setCenter(center.getWorld(), center));
    MessageKey.of("command.worldborder.center").send(sender, Prefix.CHALLENGES, format(center.getX()), format(center.getZ()));
  }

  private static void get(@NotNull CommandSender sender) {
    World world = world(sender);
    Location center = border().getCenter(world);
    MessageKey.of("command.worldborder.get").send(sender, Prefix.CHALLENGES,
      format(border().getSize(world)), format(center.getX()), format(center.getZ()));
  }

  // The console has no world of its own, so it means the overworld of the game
  @NotNull
  private static World world(@NotNull CommandSender sender) {
    if (sender instanceof Player player) return player.getWorld();
    return ChallengeAPI.getGameWorld(Environment.NORMAL);
  }

  @NotNull
  private static BorderManager border() {
    return Challenges.getInstance().getBorderManager();
  }

  @NotNull
  static String format(double value) {
    if (value == Math.rint(value)) return String.valueOf((long) value);
    return String.format(Locale.ROOT, "%.1f", value);
  }

}
