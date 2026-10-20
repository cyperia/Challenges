package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.IEntityTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RandomItemAction extends ChallengeAction {

  public RandomItemAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true).createValueChild().fill(builder -> {
      builder.addBooleanSetting("single_item",
        new ItemStack(Material.STICK), MessageKeySpace.of("custom.action.random_item.sub.single_item"),
        false
      );
    }));
  }

  public static void giveRandomItemToPlayer(@NotNull Player player, boolean singleItem) {
    InventoryUtils.giveItem(player.getInventory(),
      player.getLocation(), InventoryUtils.getRandomItem(singleItem, true));
  }

  @Override
  public void execute(
    ChallengeExecutionData executionData,
    Map<String, String[]> subActions) {

    // Presets and challenges saved before the setting existed carry no value and give a stack
    String[] singleItem = subActions.get("single_item");
    boolean single = singleItem != null && singleItem.length > 0 && singleItem[0].equals("enabled");

    for (Entity target : IEntityTargetAction.getTargets(executionData.getEntity(), subActions)) {
      if (target instanceof Player player) {
        giveRandomItemToPlayer(player, single);
      }
    }
  }

}
