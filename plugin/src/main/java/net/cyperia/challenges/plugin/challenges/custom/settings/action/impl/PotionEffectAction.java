package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class PotionEffectAction extends EntityTargetAction {

  public PotionEffectAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true).addChild(
      SubSettingsHelper.createPotionSettingsBuilder(true, true)));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    if (entity instanceof LivingEntity livingEntity) {
      try {

        PotionEffectType effectType = PotionEffectType.getByName(subActions.get("potion_type")[0]);
        if (effectType == null) return;
        PotionEffect effect = effectType.createEffect(Integer.parseInt(subActions.get("length")[0]) * 20 + 1,
          Integer.parseInt(subActions.get("amplifier")[0]));

        livingEntity.addPotionEffect(effect);
      } catch (Exception exception) {
        Challenges.getInstance().getILogger().severe("Error while adding potion effect to player");
        Challenges.getInstance().getILogger().error("", exception);
      }
    }
  }

}
