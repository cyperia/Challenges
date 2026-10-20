package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory.UncraftItemsChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;

public class UncraftInventoryAction extends EntityTargetAction {

  public UncraftInventoryAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    if (entity instanceof Player player) {
      UncraftItemsChallenge.uncraftInventory(player);
    }
  }

}
