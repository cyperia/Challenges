package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.PlayerTargetAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class HungerPlayerAction extends PlayerTargetAction {

  public HungerPlayerAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true)
      .createChooseItemChild("amount").fill(builder -> {

        for (int i = 1; i < 21; i++) {
          // TODO correct formatting
          builder.addSetting(SelectableKey.of(String.valueOf(i), new ItemStack(Material.ROTTEN_FLESH, i), LocalizableMessage.wrap(i)));
        }

      }));
  }

  @Override
  public void executeForPlayer(Player player, Map<String, String[]> subActions) {
    int newFoodLevel = player.getFoodLevel() - Integer.parseInt(subActions.get("amount")[0]);
    player.setFoodLevel(Math.max(newFoodLevel, 0));
  }

}
