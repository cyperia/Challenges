package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

public class SneakDamageChallenge extends SettingModifier {

  public SneakDamageChallenge() {
    super(1, 60);
    setValueUnit(ValueUnit.DAMAGE);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSneak(@NotNull PlayerToggleSneakEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (!event.isSneaking()) return;
    getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, event.getPlayer());
    event.getPlayer().setNoDamageTicks(0);
    event.getPlayer().damage(getValue());
    event.getPlayer().setNoDamageTicks(0);
  }

}
