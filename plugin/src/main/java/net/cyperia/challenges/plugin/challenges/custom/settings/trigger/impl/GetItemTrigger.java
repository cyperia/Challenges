package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import org.bukkit.event.EventHandler;

public class GetItemTrigger extends ChallengeTrigger {

  public GetItemTrigger(String name) {
    super(name, SubSettingsHelper.createItemSettingsBuilder());
  }

  @EventHandler
  public void onPickupItem(PlayerPickupItemEvent event) {
    createData()
      .entity(event.getPlayer())
      .data("item", event.getItem().getItemStack().getType().name())
      .execute();
  }

  @EventHandler
  public void onPlayerInventoryClick(PlayerInventoryClickEvent event) {
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getHolder() != event.getPlayer()) return;
    if (event.getCurrentItem() == null) return;
    createData()
      .entity(event.getPlayer())
      .data("item", event.getCurrentItem().getType().name())
      .execute();
  }

}
