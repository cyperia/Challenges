package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerSneakTrigger extends ChallengeTrigger {

  public PlayerSneakTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onJump(@NotNull PlayerToggleSneakEvent event) {
    if (event.isSneaking()) {
      createData().entity(event.getPlayer()).execute();
    }
  }

}
