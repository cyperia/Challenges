package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.PointsGoal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.NotNull;

public class CollectMostExpGoal extends PointsGoal {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onExpChange(@NotNull PlayerExpChangeEvent event) {
    if (!shouldExecuteEffect()) return;
    collect(event.getPlayer(), event.getAmount());
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    event.setKeepLevel(true);
  }

}
