package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class DamageEntityAction extends EntityTargetAction {

  public DamageEntityAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true).createChooseItemChild("amount").fill(builder -> {
      for (int i = 1; i < 21; i++) {
        builder.addSetting(SelectableKey.of(String.valueOf(i), new ItemStack(Material.FERMENTED_SPIDER_EYE, i), ArgumentFormat.HEARTS.apply(i)));
      }
    }));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    int amount = Integer.parseInt(subActions.get("amount")[0]);
    if (entity instanceof LivingEntity) {
      ((LivingEntity) entity).setNoDamageTicks(0);
      ((LivingEntity) entity).damage(amount);
      ((LivingEntity) entity).setNoDamageTicks(0);
    }
  }

}
