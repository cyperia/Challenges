package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LevelTeleportChallenge extends Setting {

  static int pearlsFor(int oldLevel, int newLevel) {
    return Math.max(0, newLevel - oldLevel);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    Location to = event.getTo();
    if (BlockUtils.isSameLocationIgnoreHeight(event.getFrom(), to)) return;

    // Falling and jumping stay possible, only the step to the side is taken back
    Location target = event.getFrom().clone();
    target.setY(to.getY());
    target.setYaw(to.getYaw());
    target.setPitch(to.getPitch());
    event.setTo(target);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onLevelUp(@NotNull PlayerLevelChangeEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    int pearls = pearlsFor(event.getOldLevel(), event.getNewLevel());
    if (pearls == 0) return;
    InventoryUtils.giveItem(event.getPlayer(), new ItemStack(Material.ENDER_PEARL, pearls));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onVehicleEnter(@NotNull VehicleEnterEvent event) {
    if (!(event.getEntered() instanceof Player player) || !shouldExecuteEffect(player)) return;
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMount(@NotNull EntityMountEvent event) {
    if (!(event.getEntity() instanceof Player player) || !shouldExecuteEffect(player)) return;
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDeath(@NotNull EntityDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    event.getDrops().removeIf(item -> item.getType() == Material.ENDER_PEARL);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onLoot(@NotNull LootGenerateEvent event) {
    if (!shouldExecuteEffect()) return;
    List<ItemStack> loot = new ArrayList<>(event.getLoot());
    loot.removeIf(item -> item.getType() == Material.ENDER_PEARL);
    event.setLoot(loot);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBarter(@NotNull PiglinBarterEvent event) {
    if (!shouldExecuteEffect()) return;
    event.getOutcome().removeIf(item -> item.getType() == Material.ENDER_PEARL);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onTrade(@NotNull VillagerAcquireTradeEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getRecipe().getResult().getType() == Material.ENDER_PEARL) event.setCancelled(true);
  }

}
