package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.effect.RandomPotionEffectChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class RandomPotionEffectAction extends EntityTargetAction {

  public RandomPotionEffectAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true).addChild(
      SubSettingsHelper.createPotionSettingsBuilder(false, true)));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    if (entity instanceof LivingEntity livingEntity) {
      PotionEffectType randomEffect = RandomPotionEffectChallenge
        .getNewRandomEffect(livingEntity);

      if (randomEffect == null) return;
      PotionEffect effect = randomEffect.createEffect(Integer.parseInt(subActions.get("length")[0]) * 20 + 1,
        Integer.parseInt(subActions.get("amplifier")[0]));

      livingEntity.addPotionEffect(effect);
    }
  }

}
