package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceDamageChallenge extends SettingModifier {

  public BlockPlaceDamageChallenge() {
    super(1, 60);
    setValueUnit(ValueUnit.DAMAGE);
  }

  @EventHandler
  public void onBreak(BlockPlaceEvent event) {
    if (!shouldExecuteEffect()) return;
    event.getPlayer().setNoDamageTicks(0);
    event.getPlayer().damage(getValue());
  }

}
