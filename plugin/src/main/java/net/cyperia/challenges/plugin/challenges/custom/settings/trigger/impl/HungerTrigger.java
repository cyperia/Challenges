package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerTrigger extends ChallengeTrigger {

  public HungerTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPickup(FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    if (event.getFoodLevel() < event.getEntity().getFoodLevel()) {
      createData()
        .entity(event.getEntity())
        .event(event)
        .execute();
    }
  }

}
