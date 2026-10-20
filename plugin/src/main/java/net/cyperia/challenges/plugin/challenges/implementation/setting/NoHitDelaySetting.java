package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class NoHitDelaySetting extends Setting {

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onDamageByEntity(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof LivingEntity)) return;
    if (!shouldExecuteEffect()) return;
    Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> {
      ((LivingEntity) event.getEntity()).setNoDamageTicks(0);
    }, 1);
  }

}
