package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class AlwaysRunningChallenge extends Setting {

  @ScheduledTask(ticks = 1)
  public void setVelocity() {
    broadcastFiltered(player -> {
      if (player.isSprinting()) return;

      Location location = player.getLocation();
      Vector velocity = new Vector();

      double rotX = location.getYaw();
      double xz = Math.cos(Math.toRadians(0));
      velocity.setX(-xz * Math.sin(Math.toRadians(rotX)));
      velocity.setZ(xz * Math.cos(Math.toRadians(rotX)));
      velocity.multiply(0.5);

      if (player.isSneaking())
        velocity.multiply(0.5);
      else if (player.isInWater())
        velocity.multiply(0.5);
      else if (!player.isOnGround())
        velocity.multiply(0.85);

      Vector oldVelocity = player.getVelocity();
      if (oldVelocity.getY() > 0)
        oldVelocity.multiply(0.9);
      velocity.setY(oldVelocity.getY());
      player.setVelocity(velocity);
    });
  }

}
