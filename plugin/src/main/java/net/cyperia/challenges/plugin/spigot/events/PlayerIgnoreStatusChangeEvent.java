package net.cyperia.challenges.plugin.spigot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerIgnoreStatusChangeEvent extends PlayerEvent {

  private static final HandlerList handlers = new HandlerList();

  private final boolean isIgnored;

  public PlayerIgnoreStatusChangeEvent(@NotNull Player who, boolean isIgnored) {
    super(who);
    this.isIgnored = isIgnored;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  public boolean isIgnored() {
    return isIgnored;
  }

  public boolean isNotIgnored() {
    return !isIgnored;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
