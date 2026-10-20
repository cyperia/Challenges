package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

public class OneDurabilityChallenge extends Setting {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryClick(@NotNull InventoryClickEvent event) {
    if (!shouldExecuteEffect()) return;
    // Not every clicker is a player, so the ignore check cannot carry the effect check here
    if (event.getWhoClicked() instanceof Player player && ignorePlayer(player)) return;
    if (event.getCurrentItem() != null) setDurability(event.getCurrentItem());
    if (event.getCursor() != null) setDurability(event.getCursor());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInteract(PlayerInteractEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getItem() == null) return;
    setDurability(event.getItem());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInteract(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    setDurability(event.getItem().getItemStack());
  }

  private void setDurability(@NotNull ItemStack item) {
    Damageable meta = ItemUtils.getDamageableMeta(item);
    if (meta == null) return;
    meta.setUnbreakable(false);
    meta.setDamage(item.getType().getMaxDurability() - 1);
    item.setItemMeta(meta);
  }

}
