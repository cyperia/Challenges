package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.setting.MaxHealthSetting;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ModifyMaxHealthAction extends PlayerTargetAction {

  public ModifyMaxHealthAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true)
      .createValueChild().fill(builder -> {
        builder.addModifierSetting("health_offset",
          new ItemStack(Material.RED_DYE), MessageKeySpace.of("custom.action.max_health.sub.offset"),
          0, -20, 20, ValueUnit.MAX_HEALTH.bare()
        );
      }));
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    int healthOffset = Integer.parseInt(subActions.get("health_offset")[0]);
    MaxHealthSetting instance = AbstractChallenge.getFirstInstance(MaxHealthSetting.class);

    int oldMaxHealth = instance.getMaxHealth(player);
    if (oldMaxHealth <= 0) {
      ChallengeHelper.kill(player);
      return;
    }

    instance.addHealth(player, healthOffset);

    int newMaxHealth = instance.getMaxHealth(player);
    if (newMaxHealth <= 0) {
      ChallengeHelper.kill(player);
    }
  }

}
