package net.cyperia.challenges.plugin.spigot.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

// Only the location may be changed here, the player is not respawned yet
public class PlayerRespawnLocationEvent extends PlayerEvent {

  private static final HandlerList handlers = new HandlerList();

  private Location respawnLocation;

  // Bukkit accepts a sync event on a region thread only, and an async one everywhere else
  public PlayerRespawnLocationEvent(@NotNull Player who, @NotNull Location respawnLocation) {
    super(who, !Bukkit.isPrimaryThread());
    this.respawnLocation = respawnLocation;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @NotNull
  public Location getRespawnLocation() {
    return respawnLocation;
  }

  public void setRespawnLocation(@NotNull Location respawnLocation) {
    this.respawnLocation = respawnLocation;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
