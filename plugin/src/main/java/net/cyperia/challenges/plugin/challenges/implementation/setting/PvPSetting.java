package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.EntityDamageByPlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class PvPSetting extends Setting {

  public PvPSetting() {
    super(true);
  }

  @EventHandler
  public void onDamage(@NotNull EntityDamageByPlayerEvent event) {
    if (isEnabled()) return;
    if (!(event.getEntity() instanceof Player)) return;
    event.setCancelled(true);
  }

}
