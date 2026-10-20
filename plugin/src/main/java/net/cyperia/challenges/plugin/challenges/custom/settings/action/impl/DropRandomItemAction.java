package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer.RandomItemDroppingChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Player;

import java.util.Map;

public class DropRandomItemAction extends PlayerTargetAction {

  public DropRandomItemAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true));
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    RandomItemDroppingChallenge.dropRandomItem(player);
  }

}
