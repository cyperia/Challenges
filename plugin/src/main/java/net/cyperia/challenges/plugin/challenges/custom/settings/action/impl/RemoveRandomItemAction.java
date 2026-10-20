package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.entity.Player;

import java.util.Map;

public class RemoveRandomItemAction extends PlayerTargetAction {

  public RemoveRandomItemAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true));
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    InventoryUtils.removeRandomItem(player.getInventory());
  }

}
