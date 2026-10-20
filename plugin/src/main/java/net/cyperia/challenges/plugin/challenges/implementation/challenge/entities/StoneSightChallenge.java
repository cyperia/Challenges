package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class StoneSightChallenge extends Setting {

  private final Random random = new Random();

  @ScheduledTask(ticks = 1, async = false)
  public void onTick() {

    for (Player player : ChallengeHelper.getIngamePlayers()) {

      RayTraceResult result = player.getWorld().rayTraceEntities(
        player.getEyeLocation(),
        player.getLocation().getDirection(),
        30,
        0.01,
        entity -> !(entity instanceof Player) && !(entity instanceof EnderDragon) && entity instanceof LivingEntity
      );
      if (result == null) continue;

      Location location = result.getHitPosition().toLocation(player.getWorld());
      LivingEntity entity = ((LivingEntity) result.getHitEntity());
      if (entity == null) continue;

      double distance = entity.getEyeLocation().distance(location) * 5;

      BoundingBox box = entity.getBoundingBox();
      double volume = box.getWidthX() + box.getWidthZ() + box.getHeight();
      if (distance > volume) continue;

      entity.getLocation().getBlock().setType(getRandomStone(), false);
      entity.remove();
      SoundSample.BREAK.play(player);

    }

  }

  @NotNull
  private Material getRandomStone() {
    Material[] materials = {
      Material.STONE,
      Material.STONE,
      Material.STONE,
      Material.COBBLESTONE,
      Material.COBBLESTONE,
      Material.MOSSY_COBBLESTONE
    };
    return materials[random.nextInt(materials.length)];
  }

}
