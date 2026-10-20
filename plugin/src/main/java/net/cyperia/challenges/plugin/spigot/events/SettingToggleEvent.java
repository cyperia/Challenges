package net.cyperia.challenges.plugin.spigot.events;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SettingToggleEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Setting setting;

  public SettingToggleEvent(@NotNull Setting setting) {
    this.setting = setting;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @NotNull
  public Setting getSetting() {
    return setting;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

}
