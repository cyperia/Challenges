package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.Material;

public class WaterAllergyChallenge extends SettingModifier {

  public WaterAllergyChallenge() {
    super(1, 40);
    setValueUnit(ValueUnit.DAMAGE);
  }

  @ScheduledTask(ticks = 5, async = false)
  public void onFifthTick() {
    broadcastFiltered(player -> {
      if (player.getLocation().getBlock().getType() == Material.WATER) {
        player.damage(getValue());
      }
    });
  }

}
