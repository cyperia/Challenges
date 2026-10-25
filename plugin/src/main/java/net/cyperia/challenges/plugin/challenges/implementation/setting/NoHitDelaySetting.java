package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class NoHitDelaySetting extends Setting {

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onDamageByEntity(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof LivingEntity entity)) return;
    if (!shouldExecuteEffect()) return;
    Regions.later(Challenges.getInstance(), entity, () -> entity.setNoDamageTicks(0), 1);
  }

}
