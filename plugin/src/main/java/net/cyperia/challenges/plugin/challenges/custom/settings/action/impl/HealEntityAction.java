package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class HealEntityAction extends EntityTargetAction {

  public HealEntityAction(String name) {
    // TODO centralize with DamageEntityAction / value chooser
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true).createChooseItemChild("amount").fill(builder -> {
      for (int i = 1; i < 21; i++) {
        builder.addSetting(SelectableKey.of(String.valueOf(i), new ItemStack(Material.FERMENTED_SPIDER_EYE, i), ArgumentFormat.HEARTS.apply(i)));
      }
    }));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    int amount = Integer.parseInt(subActions.get("amount")[0]);
    if (entity instanceof LivingEntity livingEntity) {
      AttributeInstance attribute = livingEntity.getAttribute(Attribute.MAX_HEALTH);

      if (attribute == null) return;
      double newHealth = Math.min(livingEntity.getHealth() + amount, attribute.getBaseValue());
      livingEntity.setHealth(newHealth);
    }
  }

}
