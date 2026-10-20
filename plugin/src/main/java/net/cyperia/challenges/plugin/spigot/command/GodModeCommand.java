package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.CommandHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GodModeCommand implements SenderCommand, Completer, Listener {

  private final List<UUID> godModePlayers = new ArrayList<>();

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {

    List<Player> targets = new ArrayList<>();

    if (args.length > 0) {
      targets.addAll(CommandHelper.getPlayers(sender, args[0]));

    } else if (sender instanceof Player) {
      targets.add((Player) sender);
    }

    if (targets.isEmpty()) {
      MessageKey.of("command.no-target").send(sender, Prefix.CHALLENGES);
      return;
    }

    boolean otherPlayers = false;
    for (Player target : targets) {
      if (godModePlayers.contains(target.getUniqueId())) {
        godModePlayers.remove(target.getUniqueId());
        MessageKey.of("command.god-mode.disabled").send(target, Prefix.CHALLENGES);
      } else {
        godModePlayers.add(target.getUniqueId());
        MessageKey.of("command.god-mode.enabled").send(target, Prefix.CHALLENGES);
        target.setFoodLevel(20);
      }

      if (target != sender)
        otherPlayers = true;

    }

    if (otherPlayers)
      MessageKey.of("command.god-mode.done-others").send(sender, Prefix.CHALLENGES, targets.size());

  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    return CommandHelper.getCompletions(sender);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    Player player = (Player) event.getEntity();
    if (godModePlayers.contains(player.getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  private void onFoodLevelChange(FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    Player player = (Player) event.getEntity();
    if (godModePlayers.contains(player.getUniqueId())) {
      event.setCancelled(true);
    }
  }

}
