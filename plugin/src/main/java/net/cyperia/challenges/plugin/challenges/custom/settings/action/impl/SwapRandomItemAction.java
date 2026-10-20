package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer.RandomItemSwappingChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Player;

import java.util.Map;

public class SwapRandomItemAction extends PlayerTargetAction {

  public SwapRandomItemAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true));
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    RandomItemSwappingChallenge.swapRandomItems(player);
  }

}
