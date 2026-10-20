package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CommandHelper {

  private CommandHelper() {
  }

  public static List<String> getCompletions(CommandSender sender) {
    LinkedList<String> list = new LinkedList<>();
    for (Player player : getTargetable(sender)) {
      list.add(player.getName());
    }
    list.addAll(Arrays.asList("@a", "@r", "@p", "@s"));
    return list;
  }

  public static List<Player> getPlayers(@NotNull CommandSender sender, @NotNull String input) {
    ArrayList<Player> list = new ArrayList<>();
    Location senderLocation = getSenderLocation(sender);
    List<Player> targetable = getTargetable(sender);

    switch (input) {
      case "@a": {
        list.addAll(targetable);
        break;
      }
      case "@r": {
        if (!targetable.isEmpty()) {
          list.add(targetable.get(ThreadLocalRandom.current().nextInt(targetable.size())));
        }
        break;
      }
      case "@p": {
        if (senderLocation != null) {
          Player nearestPlayer = getNearestPlayer(senderLocation, targetable);
          if (nearestPlayer != null) list.add(nearestPlayer);
        }
        break;
      }
      case "@s": {
        if (sender instanceof Player) list.add(((Player) sender));
        break;
      }
      default: {
        Player player = Bukkit.getPlayer(input);
        if (player != null && targetable.contains(player)) list.add(player);
      }

    }

    return list;
  }

  // A player the sender cannot see is left out, so a command cannot be used to find out that
  // somebody is vanished. Console and command blocks see everyone, so they reach everyone.
  @NotNull
  private static List<Player> getTargetable(@NotNull CommandSender sender) {
    Player viewer = sender instanceof Player ? ((Player) sender) : null;
    List<Player> list = new ArrayList<>();
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (viewer == null || viewer.canSee(player)) list.add(player);
    }
    return list;
  }

  @Nullable
  public static Location getSenderLocation(@NotNull CommandSender sender) {
    if (sender instanceof Player) return ((Player) sender).getLocation();
    if (sender instanceof BlockCommandSender)
      return ((BlockCommandSender) sender).getBlock().getLocation();
    return null;
  }

  @Nullable
  public static Player getNearestPlayer(@NotNull Location location, @NotNull Collection<? extends Player> candidates) {
    if (location.getWorld() == null) return null;
    Player currentPlayer = null;
    double playersDistance = -1;

    for (Player player : candidates) {
      if (location.getWorld() != player.getWorld()) continue;
      double distance = location.distance(player.getLocation());
      if (playersDistance == -1 || distance < playersDistance) {
        currentPlayer = player;
        playersDistance = distance;
      }
    }

    return currentPlayer;
  }

}
