package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakBlockTrigger extends ChallengeTrigger {

  public BreakBlockTrigger(String name) {
    super(name, SubSettingsHelper.createBlockSettingsBuilder());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    createData()
      .entity(event.getPlayer())
      .event(event)
      .block(event.getBlock().getType())
      .execute();
  }

}
