package net.cyperia.challenges.plugin.spigot.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.implementation.setting.PregameMovementSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public class BackCommand implements ChallengeCommand, Listener {

  private final Map<UUID, List<Location>> lastLocations = new HashMap<>();

  // To check if the current teleportation is from the command
  private boolean inTeleport = false;

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("back", ChallengesPermissions.COMMAND_BACK);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root
      .executes(nodes.runAsPlayer((player, context) -> back(player, 1)))
      .then(nodes.argument("count", IntegerArgumentType.integer(1))
        .suggests(nodes.suggest(sender -> {
          int saved = sender instanceof Player player ? lastLocations.getOrDefault(player.getUniqueId(), List.of()).size() : 0;
          return IntStream.rangeClosed(1, saved).mapToObj(String::valueOf).toList();
        }))
        .executes(nodes.runAsPlayer((player, context) -> back(player, IntegerArgumentType.getInteger(context, "count")))));
  }

  private void back(@NotNull Player player, int count) {
    List<Location> list = lastLocations
      .getOrDefault(player.getUniqueId(), Lists.newArrayList());
    int savedCount = list.size();
    if (savedCount == 0) {
      MessageKey.of("command.back.none").send(player, Prefix.CHALLENGES);
      return;
    }

    int countToTeleport = Math.min(list.size(), count);
    MessageKey.of("command.back.done" + (countToTeleport > 1 ? "-multiple" : ""))
      .send(player, Prefix.CHALLENGES, countToTeleport);

    Location location = list.get(countToTeleport - 1);
    inTeleport = true;
    player.teleport(location);
    inTeleport = false;

    list.subList(0, countToTeleport).clear();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    if (inTeleport) return;

    // Don't save the teleports of the pregame movement setting
    if (!AbstractChallenge.getFirstInstance(PregameMovementSetting.class).isEnabled()) {
      if ((event.getPlayer().getGameMode() == GameMode.SURVIVAL ||
        event.getPlayer().getGameMode() == GameMode.ADVENTURE) &&
        !ChallengeAPI.isStarted()) {
        return;
      }
    }
    if (event.getTo() == null) return;
    if (event.getCause() == TeleportCause.ENDER_PEARL ||
      event.getCause() == TeleportCause.CHORUS_FRUIT) return;

    UUID uuid = event.getPlayer().getUniqueId();
    List<Location> locations = lastLocations
      .getOrDefault(uuid, Lists.newArrayList());
    lastLocations.putIfAbsent(uuid, locations);
    locations.add(0, event.getFrom());

    if (locations.size() > 30) {
      locations.remove(locations.size() - 1);
    }
  }

}
