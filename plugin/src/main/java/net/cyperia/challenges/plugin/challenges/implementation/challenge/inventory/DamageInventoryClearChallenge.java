package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class DamageInventoryClearChallenge extends SettingModifier {

  private static final int EVERYONE = 1;

  public DamageInventoryClearChallenge() {
    super(1, 2);
    setValueUnit(ChallengeHelper.createAudienceUnit(EVERYONE));
  }

  @EventHandler
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    if (!shouldExecuteEffect()) return;
    if (ChallengeHelper.finalDamageIsNull(event)) return;

    if (getValue() == EVERYONE) {
      broadcastFiltered(player -> player.getInventory().clear());
    } else {
      Player player = (Player) event.getEntity();
      player.getInventory().clear();
    }
  }

}
