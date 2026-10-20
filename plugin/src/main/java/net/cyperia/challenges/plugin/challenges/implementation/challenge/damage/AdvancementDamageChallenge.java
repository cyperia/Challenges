package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;

public class AdvancementDamageChallenge extends SettingModifier {

  public AdvancementDamageChallenge() {
    super(1, 40);
    setValueUnit(ValueUnit.DAMAGE);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerAdvancementDone(@NotNull PlayerAdvancementDoneEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getAdvancement().getKey().toString().contains(":recipes/")) return;

    event.getPlayer().setNoDamageTicks(0);
    event.getPlayer().damage(getValue());
  }

}
