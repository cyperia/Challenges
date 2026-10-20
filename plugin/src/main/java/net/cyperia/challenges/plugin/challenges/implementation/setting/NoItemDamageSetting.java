package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class NoItemDamageSetting extends Setting {

  @EventHandler
  public void onItemDamage(PlayerItemDamageEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    event.setCancelled(true);
    event.getPlayer().updateInventory();
  }

}
