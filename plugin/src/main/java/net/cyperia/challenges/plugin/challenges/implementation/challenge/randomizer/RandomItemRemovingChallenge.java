package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RandomItemRemovingChallenge extends TimedChallenge {

  public RandomItemRemovingChallenge() {
    super(1, 30, 30);
    setValueUnit(ValueUnit.INTERVAL_SECONDS);
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();

    for (Player player : ChallengeHelper.getIngamePlayers()) {
      if (player.getInventory().getContents().length == 0) continue;

      Bukkit.getScheduler().runTask(plugin, () -> {
        InventoryUtils.removeRandomItem(player.getInventory());
      });
    }
  }

}
