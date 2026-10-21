package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GodModeCommand extends PlayerTargetCommand implements Listener {

  private final List<UUID> godModePlayers = new ArrayList<>();

  public GodModeCommand() {
    super(CommandInfo.command("godmode", ChallengesPermissions.COMMAND_GODMODE).withAliases("god").asGeneric(),
      ChallengesPermissions.COMMAND_GODMODE_OTHER, "command.god-mode.done-others");
  }

  @Override
  protected void applyTo(@NotNull Player target) {
    if (godModePlayers.contains(target.getUniqueId())) {
      godModePlayers.remove(target.getUniqueId());
      MessageKey.of("command.god-mode.disabled").send(target, Prefix.CHALLENGES);
    } else {
      godModePlayers.add(target.getUniqueId());
      MessageKey.of("command.god-mode.enabled").send(target, Prefix.CHALLENGES);
      target.setFoodLevel(20);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (godModePlayers.contains(player.getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  private void onFoodLevelChange(FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (godModePlayers.contains(player.getUniqueId())) {
      event.setCancelled(true);
    }
  }

}
