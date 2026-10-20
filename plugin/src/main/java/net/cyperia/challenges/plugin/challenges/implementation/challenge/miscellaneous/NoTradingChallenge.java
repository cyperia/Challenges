package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.Sound;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

public class NoTradingChallenge extends Setting {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInteract(@NotNull PlayerInteractEntityEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getRightClicked() instanceof Villager) {
      event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
      event.setCancelled(true);
    } else if (event.getRightClicked().getType().name().equals("PIGLIN")) {
      event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PIGLIN_ADMIRING_ITEM, 1F, 1F);
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityPickupItem(@NotNull EntityPickupItemEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getEntityType().name().equals("PIGLIN")) {
      event.setCancelled(true);
    }
  }

}
