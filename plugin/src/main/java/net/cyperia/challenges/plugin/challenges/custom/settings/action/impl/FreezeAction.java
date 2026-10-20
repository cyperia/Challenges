package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.damage.FreezeChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

public class FreezeAction extends EntityTargetAction {

  public FreezeAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {

    if (entity instanceof LivingEntity) {
      LivingEntity livingEntity = (LivingEntity) entity;
      AbstractChallenge.getFirstInstance(FreezeChallenge.class).setFreeze(livingEntity, 2);
    }

  }

}
