package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.EntityDamageByPlayerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class ReversedDamageChallenge extends Setting {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDamageByPlayer(@NotNull EntityDamageByPlayerEvent event) {
    if (!shouldExecuteEffect(event.getDamager())) return;

    double damage = event.getFinalDamage();
    event.getDamager().damage(damage);
  }

}
