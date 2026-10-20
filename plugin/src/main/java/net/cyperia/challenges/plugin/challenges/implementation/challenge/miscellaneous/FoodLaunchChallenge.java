package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.utils.misc.EntityUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FoodLaunchChallenge extends SettingModifier {

  public FoodLaunchChallenge() {
    super(1, 10, 2);
    setValueUnit(ValueUnit.STRENGTH);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    Vector velocityToAdd = new Vector(0, getValue() / 2, 0);
    Vector newVelocity = EntityUtils.getSucceedingVelocity(event.getPlayer().getVelocity()).add(velocityToAdd);
    event.getPlayer().setVelocity(newVelocity);
  }

}
