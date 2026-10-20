package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class BlockFlyInAirChallenge extends Setting {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;

    Block blockBelow = BlockUtils.getBlockBelow(event.getTo());
    if (blockBelow == null) return;
    if (!blockBelow.getType().isSolid()) return;

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      boostBlockInAir(blockBelow);
    }, 20);

  }

  private void boostBlockInAir(Block block) {
    if (!block.getType().isSolid()) return;

    FallingBlock fallingBlock = block.getWorld()
      .spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), block.getBlockData());
    fallingBlock.setInvulnerable(true);
    fallingBlock.setVelocity(new Vector(0, 1, 0));

    block.setType(Material.AIR);
  }


}
