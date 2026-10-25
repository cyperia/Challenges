package net.cyperia.challenges.plugin.spigot.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerRespawnedEvent extends PlayerEvent {

  private static final HandlerList handlers = new HandlerList();

  // Bukkit accepts a sync event on a region thread only, and an async one everywhere else
  public PlayerRespawnedEvent(@NotNull Player who) {
    super(who, !Bukkit.isPrimaryThread());
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
