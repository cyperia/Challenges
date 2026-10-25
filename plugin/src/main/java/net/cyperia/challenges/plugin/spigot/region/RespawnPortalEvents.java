package net.cyperia.challenges.plugin.spigot.region;

import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnLocationEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnedEvent;
import net.cyperia.challenges.plugin.spigot.events.PortalArrivedEvent;
import net.cyperia.challenges.plugin.spigot.events.PortalTravelEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fires the plugin's own respawn and portal events for whichever platform events an adapter is bound to.
 * Bukkit events are never fired a second time, so other plugins see each one once.
 */
public final class RespawnPortalEvents {

  private RespawnPortalEvents() {
  }

  /**
   * @return the location the listeners settled on, the same instance when none changed it
   */
  @NotNull
  public static Location respawnLocation(@NotNull Player player, @NotNull Location location) {
    PlayerRespawnLocationEvent event = new PlayerRespawnLocationEvent(player, location);
    Bukkit.getPluginManager().callEvent(event);
    return event.getRespawnLocation();
  }

  public static void respawned(@NotNull Player player) {
    Bukkit.getPluginManager().callEvent(new PlayerRespawnedEvent(player));
  }

  @NotNull
  public static PortalTravelEvent portalTravel(@NotNull Entity entity, @NotNull World from, @NotNull World targetWorld,
                                               @NotNull PortalType type, @Nullable Location destination, boolean cancelled) {
    PortalTravelEvent event = new PortalTravelEvent(entity, from, targetWorld, type, destination);
    event.setCancelled(cancelled);
    Bukkit.getPluginManager().callEvent(event);
    return event;
  }

  public static void portalArrived(@NotNull Entity entity, @NotNull World from, @NotNull PortalType type) {
    Bukkit.getPluginManager().callEvent(new PortalArrivedEvent(entity, from, type));
  }

}
