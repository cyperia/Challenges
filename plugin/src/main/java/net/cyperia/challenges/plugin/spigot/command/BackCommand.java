package net.cyperia.challenges.plugin.spigot.command;

import com.google.common.collect.Lists;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.implementation.setting.PregameMovementSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackCommand implements PlayerCommand, TabCompleter, Listener {

  private final Map<UUID, List<Location>> lastLocations = new HashMap<>();

  // To check if the current teleportation is from the command
  private boolean inTeleport = false;

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {

    int count = 1;
    try {
      if (args.length > 0) {
        count = Integer.parseInt(args[0]);
      }
    } catch (NumberFormatException formatException) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "back [count]");
      return;
    }

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

    if (countToTeleport > 0) {
      list.subList(0, countToTeleport).clear();
    }
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) {
    if (!(sender instanceof Player)) return Lists.newLinkedList();
    Player player = (Player) sender;

    LinkedList<String> list = Lists.newLinkedList();
    for (int i = 0; i < lastLocations.getOrDefault(player.getUniqueId(),
      Lists.newLinkedList()).size(); i++) {
      list.add(String.valueOf(i + 1));
    }
    return list;
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
