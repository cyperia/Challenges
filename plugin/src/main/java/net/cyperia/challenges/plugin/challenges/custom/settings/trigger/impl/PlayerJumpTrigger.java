package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.spigot.events.PlayerJumpEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class PlayerJumpTrigger extends ChallengeTrigger {

  public PlayerJumpTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onJump(@NotNull PlayerJumpEvent event) {
    createData().entity(event.getPlayer()).execute();
  }

}
