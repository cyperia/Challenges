package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class PlayerGlowSetting extends Setting {

  @Override
  protected void onEnable() {
    updateEffects();
  }

  @Override
  protected void onDisable() {
    updateEffects();
  }

  @TimerTask(status = {TimerStatus.PAUSED, TimerStatus.RUNNING}, async = false)
  @ScheduledTask(ticks = 50, async = false, timerPolicy = TimerPolicy.ALWAYS)
  public void updateEffects() {
    if (!shouldExecuteEffect()) {
      broadcastAudience(player -> player.removePotionEffect(PotionEffectType.GLOWING));
      return;
    }
    broadcastFiltered(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 1, true, false, false)));
  }

  @EventHandler
  public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    updateEffects();
  }

}
