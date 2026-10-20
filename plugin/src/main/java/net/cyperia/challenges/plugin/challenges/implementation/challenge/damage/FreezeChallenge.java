package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import org.bukkit.potion.PotionEffectType;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class FreezeChallenge extends SettingModifier {

  public FreezeChallenge() {
    super(5, 60, 20);
    setValueUnit(ValueUnit.SECONDS);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    if (ChallengeHelper.finalDamageIsNull(event)) return;
    double damage = ChallengeHelper.getFinalDamage(event);

    setFreeze(player, damage);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;
    if (event.getTo().getY() == event.getFrom().getY() && event.getTo().getX() == event.getFrom().getX() && event.getTo().getZ() == event.getFrom().getZ())
      return;
    PotionEffect effect = event.getPlayer().getPotionEffect(PotionEffectType.SLOWNESS);
    if (effect == null || effect.getAmplifier() != 255) return;
    event.setCancelled(true);
  }

  public void setFreeze(LivingEntity entity, double damage) {
    int time = (int) (damage * getValue() * 20 / 2);
    PotionEffect effect = entity.getPotionEffect(PotionEffectType.SLOWNESS);
    entity.removePotionEffect(PotionEffectType.SLOWNESS);

    if (effect != null && effect.getAmplifier() == 255) time += effect.getDuration();

    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, time, 255));
  }

}
