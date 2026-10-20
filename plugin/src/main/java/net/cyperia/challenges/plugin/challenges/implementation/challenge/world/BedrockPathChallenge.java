package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class BedrockPathChallenge extends Setting {

  // Checked every tick as well as on every move: a teleport fires no move, and the landing between two
  // jumps can last a single tick that the moves of a lagging client arrive around.
  @ScheduledTask(ticks = 1, async = false)
  public void checkGround() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      pave(player, player.getLocation());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect() || !event.hasChangedPosition()) return;
    pave(event.getPlayer(), event.getTo());
  }

  private void pave(@NotNull Player player, @NotNull Location at) {
    if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

    Block ground = BlockUtils.getSolidGround(player, at);
    if (ground == null || ground.getType() == Material.BEDROCK) return;

    BlockUtils.setBlockNatural(ground, Material.BEDROCK, true);
  }

}
