package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.PointsGoal;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class MostEmeraldsGoal extends PointsGoal {

  @Override
  protected void onEnable() {
    broadcastFiltered(this::updatePoints);
    super.onEnable();
  }

  private void updatePoints(@NotNull Player player) {
    Bukkit.getScheduler().runTask(plugin, () -> {
      int count = getEmeraldsCount(player);
      setPoints(player.getUniqueId(), count);
    });
  }

  private int getEmeraldsCount(@NotNull Player player) {
    PlayerInventory inventory = player.getInventory();
    int count = 0;
    for (ItemStack itemStack : inventory.getContents()) {
      if (itemStack != null && itemStack.getType() == Material.EMERALD)
        count += itemStack.getAmount();
    }
    if (player.getItemOnCursor().getType() == Material.EMERALD)
      count += player.getItemOnCursor().getAmount();
    return count;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUpdate(@NotNull InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    updatePoints(player);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUpdate(@NotNull PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    updatePoints(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUpdate(@NotNull PlayerDropItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    updatePoints(event.getPlayer());
  }

}
