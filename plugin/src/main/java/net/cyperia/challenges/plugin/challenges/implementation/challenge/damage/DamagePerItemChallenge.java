package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;

public class DamagePerItemChallenge extends Setting {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPickup(@NotNull PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    applyDamage(event.getPlayer(), event.getItem().getItemStack().getAmount());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClick(@NotNull PlayerInventoryClickEvent event) {
    if (event.isCancelled()) return; // ignoreCancelled not working on own event
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getAction() == InventoryAction.NOTHING) return;
    if (event.getCurrentItem() == null) return;
    applyDamage(event.getPlayer(), event.getCurrentItem().getAmount());
  }

  private void applyDamage(@NotNull Player player, int amount) {
    player.setNoDamageTicks(0);
    player.damage(amount);
  }

}
