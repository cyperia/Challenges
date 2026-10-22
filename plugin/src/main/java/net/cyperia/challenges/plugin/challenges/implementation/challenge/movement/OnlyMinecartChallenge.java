package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.NotNull;

public class OnlyMinecartChallenge extends Setting {

  // Puts back everyone who lost their cart: on start, after a respawn, after an ender pearl
  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    if (!shouldExecuteEffect()) return;
    broadcastFiltered(player -> {
      if (player.isDead() || player.getVehicle() instanceof Minecart) return;
      player.leaveVehicle();
      player.getWorld().spawn(player.getLocation(), RideableMinecart.class, cart -> cart.addPassenger(player));
    });
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExit(@NotNull VehicleExitEvent event) {
    if (!(event.getVehicle() instanceof Minecart)) return;
    if (!(event.getExited() instanceof Player player) || !shouldExecuteEffect(player)) return;
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDamage(@NotNull VehicleDamageEvent event) {
    if (carriesPlayer(event.getVehicle())) event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDestroy(@NotNull VehicleDestroyEvent event) {
    if (carriesPlayer(event.getVehicle())) event.setCancelled(true);
  }

  private boolean carriesPlayer(@NotNull Vehicle vehicle) {
    if (!(vehicle instanceof Minecart)) return false;
    for (Entity passenger : vehicle.getPassengers()) {
      if (passenger instanceof Player player && shouldExecuteEffect(player)) return true;
    }
    return false;
  }

}
