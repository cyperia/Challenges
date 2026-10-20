package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class InvisibleMobsChallenge extends Setting {

  @Override
  protected void onEnable() {
    addEffectForEveryEntity();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawn(@NotNull EntitySpawnEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;
    addEffect(((LivingEntity) event.getEntity()));
  }

  @ScheduledTask(ticks = 20, async = false, timerPolicy = TimerPolicy.ALWAYS)
  public void playEffects() {
    if (!shouldExecuteEffect()) return;
    addEffectForEveryEntity();
  }

  private void addEffectForEveryEntity() {
    for (World world : ChallengeAPI.getGameWorlds()) {
      for (LivingEntity entity : world.getLivingEntities()) {
        if (entity instanceof Player) continue;
        addEffect(entity);
      }
    }
  }

  private void addEffect(@NotNull LivingEntity entity) {
    entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1, true, false, false));
  }

}
