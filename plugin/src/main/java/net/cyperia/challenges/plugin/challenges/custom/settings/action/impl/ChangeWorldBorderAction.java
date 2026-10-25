package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import net.cyperia.commons.bukkit.utils.misc.Regions;

import java.util.Map;

public class ChangeWorldBorderAction extends ChallengeAction {

  public ChangeWorldBorderAction(String name) {
    super(name, SubSettingsBuilder.createValueItem()
      .addModifierSetting(
        "change",
        new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA), MessageKeySpace.of("custom.action.modify_border.sub.change"),
        1, -10, 10, ValueUnit.BLOCKS
      ));
  }

  @Override
  public void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {

    try {
      int change = Integer.parseInt(subActions.get("change")[0]);
      Regions.global(Challenges.getInstance(), () -> Challenges.getInstance().getBorderManager().changeSize(change));
    } catch (NumberFormatException | IndexOutOfBoundsException ex) {
      Logger.error("Error while accessing change border action values", ex);
    }

  }

}
