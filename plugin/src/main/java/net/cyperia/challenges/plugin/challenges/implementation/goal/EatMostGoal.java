package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.PointsGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EatMostGoal extends PointsGoal {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onItemConsume(FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    int changedFoodLevel = event.getFoodLevel() - event.getEntity().getFoodLevel();
    if (changedFoodLevel > 0) {
      addPoints(event.getEntity().getUniqueId(), changedFoodLevel);
      MessageKey.of("challenge.generic.points").send(event.getEntity(), Prefix.CHALLENGES, "+" + changedFoodLevel);
    }
  }

}
