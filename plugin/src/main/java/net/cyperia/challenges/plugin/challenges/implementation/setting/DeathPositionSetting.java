package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class DeathPositionSetting extends Setting {

  private static final String POSITION_PREFIX = "death-";

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDeath(@NotNull PlayerDeathEvent event) {
    if (!shouldExecuteEffect()) return;

    PositionSetting setting = AbstractChallenge.getFirstInstance(PositionSetting.class);
    int index = 1;
    while (setting.containsPosition(POSITION_PREFIX + index)) {
      index++;
    }

    Player player = event.getEntity();
    setting.setPosition(POSITION_PREFIX + index, player);
  }

}
