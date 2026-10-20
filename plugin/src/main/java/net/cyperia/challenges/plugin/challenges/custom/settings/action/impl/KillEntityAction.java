package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class KillEntityAction extends EntityTargetAction {

  public KillEntityAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    if (entity instanceof Player) {
      ChallengeHelper.kill(((Player) entity));
    } else if (entity instanceof LivingEntity) {
      ((LivingEntity) entity).damage(((LivingEntity) entity).getHealth());
    }
  }

}
