package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class WeatherCommand implements ChallengeCommand {

  public enum Weather {
    CLEAR, RAIN, THUNDER
  }

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("weather", ChallengesPermissions.COMMAND_WEATHER).asGeneric();
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    for (String name : List.of("clear", "sun")) {
      root.then(nodes.literal(name).executes(nodes.runAsPlayer((player, context) -> set(player, Weather.CLEAR))));
    }
    root.then(nodes.literal("rain").executes(nodes.runAsPlayer((player, context) -> set(player, Weather.RAIN))));
    root.then(nodes.literal("thunder").executes(nodes.runAsPlayer((player, context) -> set(player, Weather.THUNDER))));
  }

  @NotNull
  public static ShortcutCommand shortcut(@NotNull String literal, @NotNull Weather weather) {
    return ShortcutCommand.forPlayer(CommandInfo.command(literal, ChallengesPermissions.COMMAND_WEATHER).asGeneric(),
      (player, context) -> set(player, weather));
  }

  private static void set(@NotNull Player player, @NotNull Weather weather) {
    World world = player.getWorld();
    world.setStorm(weather != Weather.CLEAR);
    world.setThundering(weather == Weather.THUNDER);
    MessageKey.of("command.weather." + weather.name().toLowerCase(Locale.ROOT)).send(player, Prefix.CHALLENGES);
  }

}
