package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

public class ExtraWorldRestrictionListener implements Listener {

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockPlace(@NotNull BlockPlaceEvent event) {
    if (!isInExtraWorld(event.getBlock().getLocation())) return;
    if (Challenges.getInstance().getWorldManager().getSettings().isPlaceBlocks()) return;

    event.setBuild(false);
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockBreak(@NotNull BlockBreakEvent event) {
    if (!isInExtraWorld(event.getBlock().getLocation())) return;
    if (Challenges.getInstance().getWorldManager().getSettings().isDestroyBlocks()) return;

    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onDrop(@NotNull PlayerDropItemEvent event) {
    if (!isInExtraWorld(event.getPlayer().getWorld())) return;
    if (Challenges.getInstance().getWorldManager().getSettings().isDropItems()) return;

    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPickUp(@NotNull PlayerPickupItemEvent event) {
    if (!isInExtraWorld(event.getPlayer().getWorld())) return;
    if (Challenges.getInstance().getWorldManager().getSettings().isPickupItems()) return;

    event.setCancelled(true);
  }

  private boolean isInExtraWorld(@NotNull Location location) {
    if (location.getWorld() == null) return false;
    return isInExtraWorld(location.getWorld());
  }

  private boolean isInExtraWorld(@NotNull World world) {
    return Challenges.getInstance().getWorldManager().getExtraWorld().equals(world);
  }

}
