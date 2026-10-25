package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.misc.Regions;

public class RandomItemRemovingChallenge extends TimedChallenge {

  public RandomItemRemovingChallenge() {
    super(1, 30, 30);
    setValueUnit(ValueUnit.INTERVAL_SECONDS);
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();

    broadcastFiltered(player -> {
      if (player.getInventory().getContents().length == 0) return;

      Regions.nextTick(plugin, player, () -> {
        InventoryUtils.removeRandomItem(player.getInventory());
      });
    });
  }

}
