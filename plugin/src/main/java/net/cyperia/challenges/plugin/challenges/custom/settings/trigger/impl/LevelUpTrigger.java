package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class LevelUpTrigger extends ChallengeTrigger {

  public LevelUpTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onLevelUp(PlayerLevelChangeEvent event) {
    if (event.getNewLevel() > event.getOldLevel()) {
      createData()
        .entity(event.getPlayer())
        .execute();
    }
  }

}
