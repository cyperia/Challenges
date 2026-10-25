package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingGoal;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EatCakeGoal extends SettingGoal {

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInteract(PlayerInteractEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getClickedBlock() == null) return;
    if (event.getClickedBlock().getType() != Material.CAKE) return;
    if (event.getPlayer().getFoodLevel() >= 20) return;
    // Execute on next tick to prevent hunger bar not filling up
    Regions.nextTick(plugin, () -> {
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED, () -> Collections.singletonList(event.getPlayer()));
    });
  }

}
