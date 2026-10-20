package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementTrigger extends ChallengeTrigger {

  public AdvancementTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPickup(PlayerAdvancementDoneEvent event) {
    if (event.getAdvancement().getKey().toString().contains(":recipes/")) return;
    createData().entity(event.getPlayer()).execute();
  }

}
