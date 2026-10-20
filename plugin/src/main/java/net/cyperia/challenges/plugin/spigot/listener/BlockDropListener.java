package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BlockDropListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockBreak(@NotNull BlockBreakEvent event) {
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
    if (!event.isDropItems()) return;
    Player player = event.getPlayer();
    dropCustomDrops(event.getBlock(), player.getInventory().getItemInMainHand(), player, () -> event.setDropItems(false));
  }

  // blocks that lose their support, like the sugar cane above a broken one, break without a BlockBreakEvent
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockDestroy(@NotNull BlockDestroyEvent event) {
    if (!event.willDrop()) return;
    dropCustomDrops(event.getBlock(), null, null, () -> event.setWillDrop(false));
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockExplosion(@NotNull BlockExplodeEvent event) {
    handleExplosion(event.blockList(), () -> event.setYield(0));
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityExplosion(@NotNull EntityExplodeEvent event) {
    handleExplosion(event.blockList(), () -> event.setYield(0));
  }

  protected void handleExplosion(@NotNull Iterable<Block> blocklist, @NotNull Runnable dropsExist) {
    for (Block block : blocklist) {
      dropCustomDrops(block, null, null, dropsExist);
    }
  }

  protected void dropCustomDrops(@NotNull Block block, @Nullable ItemStack tool, @Nullable Entity breaker, @NotNull Runnable dropsExist) {

    Material material = block.getType();
    if (material.isAir()) return;

    if (!ChallengeAPI.getDropChance(material)) {
      dropsExist.run();
      return;
    }

    Collection<ItemStack> drops = ChallengeAPI.getCustomDrops(block, tool, breaker);
    if (drops == null) return;

    Location location = block.getLocation().clone().add(0.5, 0, 0.5);
    if (location.getWorld() == null) return;

    dropsExist.run();
    for (ItemStack drop : drops) {
      try {
        location.getWorld().dropItem(location, drop);
      } catch (Exception ex) {
        Logger.warn("Unable to drop custom drop {}", drop, ex);
      }
    }

  }

}
