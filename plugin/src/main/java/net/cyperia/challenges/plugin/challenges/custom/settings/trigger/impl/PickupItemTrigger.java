package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PickupItemTrigger extends ChallengeTrigger {

  public PickupItemTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPickup(PlayerPickupItemEvent event) {
    createData()
      .entity(event.getPlayer())
      .event(event)
      .execute();
  }

}
