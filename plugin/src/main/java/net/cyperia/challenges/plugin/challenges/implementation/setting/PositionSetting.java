package net.cyperia.challenges.plugin.challenges.implementation.setting;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.command.CommandProvider;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.utils.misc.ParticleUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class PositionSetting extends Setting implements CommandProvider {

  private final Map<String, Location> positions = new HashMap<>();
  private final boolean particleLines;

  public PositionSetting() {
    super(true);
    particleLines = plugin.getConfigDocument().getBoolean(ConfigManager.Keys.CHALLENGE_SETTINGS_POSITION_PARTICLE_LINES);
  }

  public static Environment getWorldEnvironment(@NotNull String name) {
    switch (name.toLowerCase()) {
      case "overworld":
        return Environment.NORMAL;
      case "nether":
        return Environment.NETHER;
      case "end":
        return Environment.THE_END;
      default:
        return null;
    }
  }

  @NotNull
  @Override
  public List<ChallengeCommand> getCommands() {
    return List.of(new PositionCommand(), new DelPosCommand(), new SetPosCommand());
  }

  private void showPositions(@NotNull Player player) {
    if (positions.entrySet().stream().noneMatch(entry -> player.getWorld() == entry.getValue().getWorld())) {
      getChallengeMessageKey("none").send(player, Prefix.POSITION, "position <name>");
      return;
    }

    positions.entrySet().stream()
      .filter(entry -> player.getWorld() == entry.getValue().getWorld())
      .sorted(Comparator.<Map.Entry<String, Location>>comparingDouble(entry -> entry.getValue().distance(player.getLocation())).reversed())
      .forEach(entry -> sendEntry(player, entry.getKey(), entry.getValue()));
  }

  private void showOrSetPosition(@NotNull Player player, @NotNull String name) {
    Location position = positions.get(name);
    if (position != null) {
      if (position.getWorld() != player.getLocation().getWorld()) {
        getChallengeMessageKey("other-world").send(player, Prefix.POSITION, getWorldName(position));
        SoundSample.BASS_OFF.play(player);
        return;
      }
      sendEntry(player, name, position);
      playParticleLine(player, position);
    } else if (ChallengeAPI.isPaused()) {
      MessageKey.of("timer.messages.not-started").send(player, Prefix.POSITION);
      SoundSample.BASS_OFF.play(player);
    } else {
      setPosition(name, player);
    }
  }

  private void sendEntry(@NotNull Player player, @NotNull String name, @NotNull Location location) {
    getChallengeMessageKey("entry").send(player, Prefix.POSITION, location.getBlockX(), location.getBlockY(), location.getBlockZ(), getWorldName(location), name, (int) location.distance(player.getLocation()));
  }

  private boolean rejectDisabled(@NotNull Player player) {
    if (isEnabled()) return false;
    getChallengeMessageKey("disabled").send(player, Prefix.POSITION);
    SoundSample.BASS_OFF.play(player);
    return true;
  }

  private boolean rejectNotStarted(@NotNull Player player) {
    if (ChallengeAPI.isStarted()) return false;
    MessageKey.of("timer.messages.not-started").send(player, Prefix.POSITION);
    SoundSample.BASS_OFF.play(player);
    return true;
  }

  @NotNull
  private <S> SuggestionProvider<S> positionNames(@NotNull CommandNodes<S> nodes) {
    return nodes.suggest(sender -> positions.keySet());
  }

  public String getWorldName(@NotNull Location location) {
    if (location.getWorld() == null) return "?";
    switch (location.getWorld().getEnvironment()) {
      case NETHER:
        return "Nether";
      case THE_END:
        return "End";
      default:
        return "Overworld";
    }
  }

  public boolean containsPosition(@NotNull String name) {
    return positions.containsKey(name);
  }

  public void setPosition(@NotNull String name, @NotNull Player player) {
    Location position = player.getLocation();
    positions.put(name, position);
    getChallengeMessageKey("created").broadcast(Prefix.POSITION, position.getBlockX(), position.getBlockY(), position.getBlockZ(), getWorldName(position), name, player);
    SoundSample.BASS_ON.play(player);
    broadcastParticleLine(position);
  }

  private void broadcastParticleLine(@NotNull Location location) {
    broadcastAudience(player -> playParticleLine(player, location));
  }

  private void playParticleLine(@NotNull Player player, @NotNull Location position) {
    if (!particleLines) return;
    if (player.getWorld() != position.getWorld()) return;

    // Defining target location to
    Location target = position.clone().add(0, 0.3, 0);

    final int[] current = {0};
    Bukkit.getScheduler().runTaskTimer(plugin, task -> {
      current[0]++;
      if (current[0] >= 10) task.cancel();
      ParticleUtils.drawLine(player, player.getLocation(), target,
        Particle.DUST, new DustOptions(Color.LIME, 1), 1, 0.5, 50);
    }, 0, 10);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    positions.clear();
    for (String name : document.keys()) {
      Location position = document.getSerializable(name, Location.class);
      if (position != null) positions.put(name, position);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    for (String key : document.keys()) {
      document.remove(key);
    }
    positions.forEach(document::set);
  }

  private class PositionCommand implements ChallengeCommand {

    @NotNull
    @Override
    public CommandInfo info() {
      return CommandInfo.command("position", ChallengesPermissions.COMMAND_POSITION).withAliases("pos", "coords");
    }

    @Override
    public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      root
        .executes(nodes.runAsPlayer((player, context) -> {
          if (rejectDisabled(player)) return;
          showPositions(player);
        }))
        .then(nodes.argument("name", StringArgumentType.word())
          .suggests(positionNames(nodes))
          .executes(nodes.runAsPlayer((player, context) -> {
            if (rejectDisabled(player)) return;
            showOrSetPosition(player, StringArgumentType.getString(context, "name").toLowerCase(Locale.ROOT));
          })));
    }

  }

  private class DelPosCommand implements ChallengeCommand {

    @NotNull
    @Override
    public CommandInfo info() {
      return CommandInfo.command("delposition", ChallengesPermissions.COMMAND_DELPOSITION).withAliases("delpos", "delcoords");
    }

    @Override
    public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      root
        .executes(nodes.runAsPlayer((player, context) -> {
          if (rejectDisabled(player) || rejectNotStarted(player)) return;
          if (positions.isEmpty()) {
            getChallengeMessageKey("none-global").send(player, Prefix.POSITION, "position <name>");
            return;
          }

          positions.entrySet().stream()
            .sorted(Comparator.<Map.Entry<String, Location>>comparingDouble(entry -> entry.getValue().distance(player.getLocation())).reversed())
            .forEach(entry -> sendEntry(player, entry.getKey(), entry.getValue()));
        }))
        .then(nodes.argument("name", StringArgumentType.word())
          .suggests(positionNames(nodes))
          .executes(nodes.runAsPlayer((player, context) -> {
            if (rejectDisabled(player) || rejectNotStarted(player)) return;
            String name = StringArgumentType.getString(context, "name").toLowerCase(Locale.ROOT);
            Location position = positions.remove(name);
            if (position == null) {
              getChallengeMessageKey("not-exists").send(player, Prefix.POSITION);
              return;
            }
            getChallengeMessageKey("deleted").broadcast(Prefix.POSITION,
              position.getBlockX(), position.getBlockY(), position.getBlockZ(),
              getWorldName(position), name, player);
          })));
    }

  }

  private class SetPosCommand implements ChallengeCommand {

    @NotNull
    @Override
    public CommandInfo info() {
      return CommandInfo.command("setposition", ChallengesPermissions.COMMAND_SETPOSITION).withAliases("setpos", "setcoords");
    }

    @Override
    public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      RequiredArgumentBuilder<S, String> name = nodes.argument("name", StringArgumentType.word());
      for (String world : new String[]{"overworld", "nether", "end"}) {
        name.then(nodes.literal(world)
          .then(nodes.argument("x", DoubleArgumentType.doubleArg()).suggests(coordinate(nodes, location -> location.getBlockX() + 0.5))
            .then(nodes.argument("y", DoubleArgumentType.doubleArg()).suggests(coordinate(nodes, Location::getBlockY))
              .then(nodes.argument("z", DoubleArgumentType.doubleArg()).suggests(coordinate(nodes, location -> location.getBlockZ() + 0.5))
                .executes(nodes.runAsPlayer((player, context) -> setPosition(player,
                  StringArgumentType.getString(context, "name").toLowerCase(Locale.ROOT), world,
                  DoubleArgumentType.getDouble(context, "x"), DoubleArgumentType.getDouble(context, "y"), DoubleArgumentType.getDouble(context, "z"))))))));
      }
      root.then(name);
    }

    private void setPosition(@NotNull Player player, @NotNull String name, @NotNull String worldName, double x, double y, double z) {
      if (rejectDisabled(player) || rejectNotStarted(player)) return;
      if (positions.containsKey(name)) {
        getChallengeMessageKey("already-exists").send(player, Prefix.POSITION, name);
        return;
      }

      World world = ChallengeAPI.getGameWorld(getWorldEnvironment(worldName));
      if (world == null) {
        MessageKey.of("command.world.not-loaded").send(player, Prefix.POSITION);
        return;
      }

      Location position = new Location(world, x, y, z);
      positions.put(name, position);
      getChallengeMessageKey("created").broadcast(Prefix.POSITION, position.getBlockX(), position.getBlockY(), position.getBlockZ(), getWorldName(position), name, player);
      SoundSample.BASS_ON.play(player);
      broadcastParticleLine(position);
    }

    @NotNull
    private <S> SuggestionProvider<S> coordinate(@NotNull CommandNodes<S> nodes, @NotNull ToDoubleFunction<Location> axis) {
      return nodes.suggest(sender -> sender instanceof Player player ? List.of(String.valueOf(axis.applyAsDouble(player.getLocation()))) : List.of());
    }

  }

}
