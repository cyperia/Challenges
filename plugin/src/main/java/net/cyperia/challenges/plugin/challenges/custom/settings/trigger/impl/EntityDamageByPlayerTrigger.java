package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.spigot.events.EntityDamageByPlayerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class EntityDamageByPlayerTrigger extends ChallengeTrigger {

  public EntityDamageByPlayerTrigger(String name) {
    super(name, SubSettingsHelper.createEntityTypeSettingsBuilder(true, true));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDeath(@NotNull EntityDamageByPlayerEvent event) {

    createData()
      .entity(event.getDamager())
      .event(event)
      .entityType(event.getEntityType())
      .execute();

  }

}
