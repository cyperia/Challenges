package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class GainXPTrigger extends ChallengeTrigger {

  public GainXPTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onXPGain(PlayerExpChangeEvent event) {
    if (event.getAmount() > 0) {
      createData()
        .entity(event.getPlayer())
        .cancelAction(() -> event.setAmount(0))
        .execute();
    }
  }

}
