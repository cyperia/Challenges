package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveBlockTrigger extends ChallengeTrigger {

  public MoveBlockTrigger(String name) {
    super(name);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (BlockUtils.isSameBlockLocationIgnoreHeight(event.getTo(), event.getFrom())) return;
    createData()
      .entity(event.getPlayer())
      .event(event)
      .execute();
  }

}
