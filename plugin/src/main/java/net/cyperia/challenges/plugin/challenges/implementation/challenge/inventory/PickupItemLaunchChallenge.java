package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.challenges.plugin.utils.misc.EntityUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class PickupItemLaunchChallenge extends SettingModifier {

  public PickupItemLaunchChallenge() {
    super(1, 10, 2);
    setValueUnit(ValueUnit.STRENGTH);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerPickUpItem(@NotNull PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    Vector velocityToAdd = new Vector(0, getValue() / 2, 0);
    Vector newVelocity = EntityUtils.getSucceedingVelocity(event.getPlayer().getVelocity()).add(velocityToAdd);
    event.getPlayer().setVelocity(newVelocity);
  }

}
