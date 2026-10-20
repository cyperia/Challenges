package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Modifier;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotLimitSetting extends Modifier {

  /**
   * slots of the player inventory without the offhand and the armor
   */
  private static final int MAX_SLOTS = 36;

  public SlotLimitSetting() {
    super(1, MAX_SLOTS, MAX_SLOTS);
    setValueUnit(ValueUnit.VALUE.describedBy(getChallengeMessageKey("settings"), MAX_SLOTS));
  }

  @Override
  protected void onValueChange() {
    update();
  }

  @TimerTask(status = {TimerStatus.RUNNING})
  public void updateDelayed() {
    Bukkit.getScheduler().runTaskLater(plugin, this::update, 1);
  }

  @TimerTask(status = {TimerStatus.PAUSED}, async = false)
  public void update() {
    // Audience wide: an ignored player has to get their blocked slots back
    broadcastAudience(this::updateSlots);
  }

  private void updateSlots(@NotNull Player player) {
    boolean block = !ignorePlayer(player) && ChallengeAPI.isStarted();
    for (int i = 0; i < MAX_SLOTS; i++) {
      if (block && isBlocked(i)) {
        blockSlot(player, i);
      } else {
        unBlockSlot(player, i);
      }
    }
  }

  private boolean isBlocked(int slot) {
    if (slot > 35) return false;

    int value = getValue() - 1;

    if (slot >= 9 && slot <= 17) {
      slot += 9 * 2;
    } else if (slot >= 27) {
      slot -= 9 * 2;
    }

    return slot > value;
  }

  private void blockSlot(@NotNull Player player, int slot) {
    ItemStack item = player.getInventory().getItem(slot);
    if (item != null && !item.isSimilar(DefaultItems.BLOCKED_ITEM)) {
      if (!Bukkit.isPrimaryThread()) {
        Bukkit.getScheduler().runTask(plugin, () -> {
          player.getWorld().dropItemNaturally(player.getLocation(), item);
        });
      } else {
        player.getWorld().dropItemNaturally(player.getLocation(), item);
      }
    }
    player.getInventory().setItem(slot, DefaultItems.BLOCKED_ITEM);
  }

  private void unBlockSlot(@NotNull Player player, int slot) {
    ItemStack item = player.getInventory().getItem(slot);
    if (item != null && item.isSimilar(DefaultItems.BLOCKED_ITEM)) {
      player.getInventory().setItem(slot, null);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getType() != InventoryType.PLAYER) return;
    if (isBlocked(event.getSlot()) || event.getCurrentItem() != null && event.getCurrentItem().isSimilar(DefaultItems.BLOCKED_ITEM)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (!event.getItemDrop().getItemStack().isSimilar(DefaultItems.BLOCKED_ITEM)) return;
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onClick(@NotNull BlockPlaceEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (!event.getItemInHand().isSimilar(DefaultItems.BLOCKED_ITEM)) return;
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSwapItem(@NotNull PlayerSwapHandItemsEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    if (event.getMainHandItem() != null && event.getMainHandItem().isSimilar(DefaultItems.BLOCKED_ITEM)) {
      event.setCancelled(true);
    } else if (event.getOffHandItem() != null && event.getOffHandItem().isSimilar(DefaultItems.BLOCKED_ITEM)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
    event.getDrops().removeIf(itemStack -> itemStack.isSimilar(DefaultItems.BLOCKED_ITEM));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onRespawn(PlayerRespawnEvent event) {
    updateSlots(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerIgnoreStatusChange(PlayerIgnoreStatusChangeEvent event) {
    Bukkit.getScheduler().runTask(plugin, () -> {
      updateSlots(event.getPlayer());
    });
  }

}
