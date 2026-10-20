package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Modifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class DamageMultiplierModifier extends Modifier {

  public DamageMultiplierModifier() {
    super(10);
    setValueUnit(ValueUnit.MULTIPLIER);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    event.setDamage(event.getDamage() * getValue());
  }

}
