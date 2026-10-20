package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.effect.PermanentEffectOnDamageChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Player;

import java.util.Map;

public class AddPermanentEffectAction extends PlayerTargetAction {

  public AddPermanentEffectAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true));
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    PermanentEffectOnDamageChallenge instance = AbstractChallenge.getFirstInstance(PermanentEffectOnDamageChallenge.class);
    instance.addRandomEffect(player);
    instance.updateEffects();
  }

}
