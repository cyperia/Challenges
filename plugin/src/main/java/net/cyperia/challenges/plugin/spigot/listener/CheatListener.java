package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

public class CheatListener implements Listener {

  public static final String[] CHEAT_COMMANDS = {
    "give",
    "replaceitem",
    "effect",
    "i",
    "summon",
    "enchant",
    "heal",
    "kill",
    "setblock",
    "fill"
  };

  public CheatListener() {
    Challenges.getInstance().getPlayerManager().getPlayers().stream()
      .filter(player -> player.getGameMode() == GameMode.CREATIVE)
      .findFirst().ifPresent(this::handleCheatsDetected);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onGameModeChange(@NotNull PlayerGameModeChangeEvent event) {
    if (event.getNewGameMode() == GameMode.CREATIVE)
      handleCheatsDetected(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {
    String message = event.getMessage().toLowerCase();
    if (message.isEmpty()) return;

    String[] args = message.substring(1).trim().split(" ");
    String commandName = args[0];
    if (commandName.contains(":")) {
      commandName = commandName.substring(commandName.indexOf(':') + 1);
    }

    for (String command : CHEAT_COMMANDS) {
      if (!commandName.equalsIgnoreCase("/" + command)) continue;
      if (!hasPermission(event.getPlayer(), command)) continue;
      handleCheatsDetected(event.getPlayer());
      break;
    }
  }

  private boolean hasPermission(@NotNull Player player, @NotNull String command) {
    String[] prefixes = {
      "challenges.",
      "minecraft.command.",
      "essentials.",
      "bukkit."
    };

    for (String prefix : prefixes) {
      if (player.hasPermission(prefix + command))
        return true;
    }

    return false;
  }

  private void handleCheatsDetected(@NotNull Player player) {
    // A vanished staff member is not playing the run, so neither their game mode nor their commands
    // are the run being cheated.
    if (Challenges.getInstance().getPlayerManager().isVanished(player)) return;
    if (Challenges.getInstance().getServerManager().hasCheated()) return;
//    if (!Challenges.getInstance().getStatsManager().isNoStatsAfterCheating()) return; // TODO new stats system
    Challenges.getInstance().getServerManager().setHasCheated();
    Logger.info("Detected cheating: No more stats can be collected");
//    MessageKey.of("server.cheats-detected").broadcast(Prefix.CHALLENGES, player); // TODO disabled for now, no use
  }

}
