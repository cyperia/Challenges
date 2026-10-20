package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakDamageChallenge extends SettingModifier {

  public BlockBreakDamageChallenge() {
    super(1, 60);
    setValueUnit(ValueUnit.DAMAGE);
  }

  @EventHandler
  public void onBreak(BlockBreakEvent event) {
    if (!shouldExecuteEffect()) return;
    event.getPlayer().setNoDamageTicks(0);
    event.getPlayer().damage(getValue());
  }

}
