package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifierGoal;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetFullHealthGoal extends SettingModifierGoal {

  public GetFullHealthGoal() {
    super(1, 20, 20);
    setValueUnit(ValueUnit.HEARTS.describedBy(getChallengeMessageKey("settings-desc")));
  }

  @Override
  protected void onEnable() {
    broadcastFiltered(player -> {
      player.setHealth(getValue());
    });
  }

  @Override
  protected void onValueChange() {
    broadcastFiltered(player -> {
      player.setHealth(getValue());
    });
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    for (Player player : ChallengeAPI.getIngamePlayers()) {
      AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
      if (attribute != null) {
        if (player.getHealth() >= attribute.getBaseValue()) {
          winners.add(player);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHealthChange(EntityRegainHealthEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    if (!shouldExecuteEffect((Player) event.getEntity())) return;
    Player player = (Player) event.getEntity();
    Regions.nextTick(plugin, player, () -> {
      AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
      if (attribute != null && player.getHealth() >= attribute.getBaseValue()) {
        ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
      }
    });
  }

}
