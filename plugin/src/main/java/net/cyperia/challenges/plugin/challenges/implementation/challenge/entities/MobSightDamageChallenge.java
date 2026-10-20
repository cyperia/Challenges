package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

public class MobSightDamageChallenge extends SettingModifier {

  public MobSightDamageChallenge() {
    setValueUnit(ValueUnit.DAMAGE);
  }

  @ScheduledTask(ticks = 1, async = false)
  public void onTick() {

    for (Player player : ChallengeHelper.getIngamePlayers()) {

      RayTraceResult result = player.getWorld().rayTraceEntities(
        player.getEyeLocation(),
        player.getLocation().getDirection(),
        30,
        0.01,
        entity -> !(entity instanceof Player) && entity instanceof LivingEntity
      );
      if (result == null) continue;

      Location location = result.getHitPosition().toLocation(player.getWorld());
      LivingEntity entity = ((LivingEntity) result.getHitEntity());
      if (entity == null) continue;

      double distance = entity.getEyeLocation().distance(location) * 5;

      BoundingBox box = entity.getBoundingBox();
      double volume = box.getWidthX() + box.getWidthZ() + box.getHeight();
      if (distance > volume) continue;

      player.damage(getValue());
    }

  }

}
