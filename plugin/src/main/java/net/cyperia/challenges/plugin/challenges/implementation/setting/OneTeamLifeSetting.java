package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class OneTeamLifeSetting extends Setting {

  private boolean isKilling = false;

  public OneTeamLifeSetting() {
    super(true);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDeath(@NotNull PlayerDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    if (isKilling) return;

    Bukkit.getScheduler().runTask(plugin, () -> {
      try {
        AbstractChallenge.getFirstInstance(DeathMessageSetting.class).setHideMessagesTemporarily(isKilling = true);
        for (Player player : ChallengeHelper.getIngamePlayers()) {
          if (player == event.getEntity()) continue;
          ChallengeHelper.kill(player);
        }
        AbstractChallenge.getFirstInstance(RespawnSetting.class).checkAllPlayersDead();
      } finally {
        AbstractChallenge.getFirstInstance(DeathMessageSetting.class).setHideMessagesTemporarily(isKilling = false);
      }
    });

  }

}
