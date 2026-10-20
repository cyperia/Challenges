package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.misc.ParticleUtils;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PositionSetting extends Setting implements PlayerCommand, TabCompleter {

  private final Map<String, Location> positions = new HashMap<>();
  private final boolean particleLines;

  public PositionSetting() {
    super(true);
    particleLines = plugin.getConfigDocument().getBoolean(ConfigManager.Keys.CHALLENGE_SETTINGS_POSITION_PARTICLE_LINES);
    plugin.registerCommand(new DelPosCommand(), "delposition");
    plugin.registerCommand(new SetPosCommand(), "setposition");
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

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) {
    if (!isEnabled()) {
      getChallengeMessageKey("disabled").send(player, Prefix.POSITION);
      SoundSample.BASS_OFF.play(player);
      return;
    }

    if (args.length == 0) {
      if (positions.entrySet().stream().noneMatch(entry -> player.getWorld() == entry.getValue().getWorld())) {
        getChallengeMessageKey("none").send(player, Prefix.POSITION, "position <name>");
        return;
      }

      positions.entrySet().stream()
        .filter(entry -> player.getWorld() == entry.getValue().getWorld())
        .sorted(Comparator.<Map.Entry<String, Location>>comparingDouble(entry -> entry.getValue().distance(player.getLocation())).reversed())
        .forEach(entry -> {
          Location location = entry.getValue();
          getChallengeMessageKey("entry").send(player, Prefix.POSITION, location.getBlockX(), location.getBlockY(), location.getBlockZ(), getWorldName(location), entry.getKey(), (int) location.distance(player.getLocation()));
        });
    } else if (args.length == 1) {
      String name = args[0].toLowerCase();
      Location position = positions.get(name);
      if (position != null) {
        if (position.getWorld() != player.getLocation().getWorld()) {
          getChallengeMessageKey("other-world").send(player, Prefix.POSITION, getWorldName(position));
          SoundSample.BASS_OFF.play(player);
          return;
        }
        getChallengeMessageKey("entry").send(player, Prefix.POSITION, position.getBlockX(), position.getBlockY(), position.getBlockZ(), getWorldName(position), name, (int) position.distance(player.getLocation()));
        playParticleLine(player, position);
      } else if (ChallengeAPI.isPaused()) {
        MessageKey.of("timer.messages.not-started").send(player, Prefix.POSITION);
        SoundSample.BASS_OFF.play(player);
      } else {
        setPosition(name, player);
      }

    } else {
      MessageKey.of("command.syntax").send(player, Prefix.POSITION, "position [name]");
    }
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    if (args.length > 1) return new ArrayList<>();
    return Utils.filterRecommendations(args[0], positions.keySet().toArray(new String[0]));
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

  public class DelPosCommand implements PlayerCommand, TabCompleter {

    @Override
    public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
      if (!isEnabled()) {
        getChallengeMessageKey("disabled").send(player, Prefix.POSITION);
        SoundSample.BASS_OFF.play(player);
        return;
      }
      if (!ChallengeAPI.isStarted()) {
        MessageKey.of("timer.messages.not-started").send(player, Prefix.POSITION);
        SoundSample.BASS_OFF.play(player);
        return;
      }

      if (args.length == 0) {
        if (positions.isEmpty()) {
          getChallengeMessageKey("none-global").send(player, Prefix.POSITION, "position <name>");
          return;
        }

        positions.entrySet().stream()
          .sorted(Comparator.<Map.Entry<String, Location>>comparingDouble(entry -> entry.getValue().distance(player.getLocation())).reversed())
          .forEach(entry -> {
            Location location = entry.getValue();
            getChallengeMessageKey("entry").send(player, Prefix.POSITION, location.getBlockX(), location.getBlockY(), location.getBlockZ(), getWorldName(location), entry.getKey(), (int) location.distance(player.getLocation()));
          });
      } else if (args.length == 1) {
        String name = args[0].toLowerCase();
        Location position = positions.get(name);
        if (position != null) {
          positions.remove(name);
          getChallengeMessageKey("deleted").broadcast(Prefix.POSITION,
            position.getBlockX(), position.getBlockY(), position.getBlockZ(),
            getWorldName(position), name, player);
        } else {
          getChallengeMessageKey("not-exists").send(player, Prefix.POSITION);
        }

      } else {
        MessageKey.of("command.syntax").send(player, Prefix.POSITION, "delposition <name>");
      }

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (args.length > 1) return new ArrayList<>();
      return Utils.filterRecommendations(args[0], positions.keySet().toArray(new String[0]));
    }

  }

  public class SetPosCommand implements PlayerCommand, TabCompleter {

    @Override
    public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
      if (!isEnabled()) {
        getChallengeMessageKey("disabled").send(player, Prefix.POSITION);
        SoundSample.BASS_OFF.play(player);
        return;
      }
      if (!ChallengeAPI.isStarted()) {
        MessageKey.of("timer.messages.not-started").send(player, Prefix.POSITION);
        SoundSample.BASS_OFF.play(player);
        return;
      }

      if (args.length < 5) {
        MessageKey.of("command.syntax").send(player, Prefix.POSITION, "setposition <name> <world> <x> <y> <z>");
        return;
      }

      String name = args[0].toLowerCase();

      if (positions.containsKey(name)) {
        getChallengeMessageKey("already-exists").send(player, Prefix.POSITION, name);
        return;
      }

      String worldName = args[1];
      String x = args[2];
      String y = args[3];
      String z = args[4];

      try {
        Environment environment = getWorldEnvironment(worldName);
        World world = ChallengeAPI.getGameWorld(environment);
        if (world == null) throw new IllegalArgumentException();
        double doubleX = Double.parseDouble(x);
        double doubleY = Double.parseDouble(y);
        double doubleZ = Double.parseDouble(z);

        Location position = new Location(world, doubleX, doubleY, doubleZ);
        positions.put(name, position);
        getChallengeMessageKey("created").broadcast(Prefix.POSITION, position.getBlockX(), position.getBlockY(), position.getBlockZ(), getWorldName(position), name, player);
        SoundSample.BASS_ON.play(player);
        broadcastParticleLine(position);

      } catch (Exception exception) {
        MessageKey.of("command.syntax").send(player, Prefix.POSITION, "setposition <name> <world> <x> <y> <z>");
      }

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (!(sender instanceof Player player)) return new LinkedList<>();
      if (args.length > 5) return new LinkedList<>();

      if (args.length > 2) {
        int arg = args.length - 2;

        double cord;
        Location location = player.getLocation();
        switch (arg) {
          case 1:
            cord = location.getBlockX() + 0.5;
            break;
          case 2:
            cord = location.getBlockY();
            break;
          default:
            cord = location.getBlockZ() + 0.5;
        }

        return Collections.singletonList(String.valueOf(cord));

      } else if (args.length > 1) {
        return Arrays.asList("Overworld", "Nether", "End");
      }

      return new LinkedList<>();
    }

  }

}
