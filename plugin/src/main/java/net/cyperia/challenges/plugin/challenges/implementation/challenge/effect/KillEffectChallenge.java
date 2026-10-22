package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

public class KillEffectChallenge extends PermanentEffectOnDamageChallenge {

  @Override
  protected boolean givesEffect(@NotNull EntityDamageEvent event) {
    if (!(event instanceof EntityDamageByEntityEvent byEntity)) return false;
    Entity damager = byEntity.getDamager();
    if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) damager = shooter;
    return damager instanceof LivingEntity && !(damager instanceof Player);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onKill(@NotNull EntityDeathEvent event) {
    Player killer = event.getEntity().getKiller();
    if (killer == null || killer == event.getEntity()) return;
    if (!shouldExecuteEffect(killer)) return;

    addRandomEffect(killer);
    updateEffects();
  }

}
