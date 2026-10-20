package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.utils.misc.EntityUtils;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Map;

public class BoostEntityInAirAction extends EntityTargetAction {

  public BoostEntityInAirAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true).createValueChild().fill(builder -> {
      builder.addModifierSetting("strength",
        new ItemStack(Material.FEATHER), MessageKeySpace.of("custom.action.boost_in_air.sub.strength"),
        1, 1, 10, ValueUnit.STRENGTH
      );
    }));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {

    int strength = 1;
    try {
      strength = Integer.parseInt(subActions.get("strength")[0]);
    } catch (NumberFormatException exception) {
      Logger.error("", exception);
    }

    Vector velocityToAdd = new Vector(0, 1, 0).multiply(strength);
    Vector newVelocity = EntityUtils.getSucceedingVelocity(entity.getVelocity()).add(velocityToAdd);
    entity.setVelocity(newVelocity);
  }

}
