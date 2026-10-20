package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.jetbrains.annotations.NotNull;

public class TotemSaveDeathSetting extends Setting {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityResurrect(@NotNull EntityResurrectEvent event) {
    if (ChallengeHelper.isInInstantKill() && !isEnabled()) {
      event.setCancelled(true);
    }
  }

}
