package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public class NoHungerSetting extends Setting {

  @Override
  protected void onEnable() {
    broadcastFiltered(this::feedPlayer);
  }

  @EventHandler(ignoreCancelled = true)
  public void onHunger(@NotNull FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    if (!shouldExecuteEffect()) return;
    feedPlayer(((Player) event.getEntity()));
    event.setCancelled(true);
  }

  private void feedPlayer(@NotNull Player player) {
    player.setFoodLevel(20);
    player.setSaturation(20);
  }

}
