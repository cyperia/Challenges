package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Bukkit;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MoveMouseDamage extends SettingModifier {

  private final Map<UUID, Map.Entry<Float, Float>> lastView = new ConcurrentHashMap<>();

  public MoveMouseDamage() {
    super(60);
    setValueUnit(ValueUnit.DAMAGE);
  }

  @ScheduledTask(ticks = 1, async = false, timerPolicy = TimerPolicy.ALWAYS)
  public void onTick() {
    if (!shouldExecuteEffect()) {
      lastView.clear();
      return;
    }

    plugin.getPlayerManager().forEachOnOwnThread(Bukkit.getOnlinePlayers(), player -> {
      if (ignorePlayer(player)) {
        lastView.remove(player.getUniqueId());
        return;
      }
      if (player.getNoDamageTicks() > 0) return;

      float yaw = player.getLocation().getYaw();
      float pitch = player.getLocation().getPitch();
      Map.Entry<Float, Float> pair = lastView.getOrDefault(player.getUniqueId(), new SimpleEntry<>(yaw, pitch));
      lastView.put(player.getUniqueId(), pair);

      if (yaw != pair.getKey() || pitch != pair.getValue()) {
        Regions.nextTick(plugin, player, () -> {
          if (player.getNoDamageTicks() > 0) return;
          getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, player);
          player.damage(getValue());
          player.setNoDamageTicks(5);
          Regions.later(plugin, () -> lastView.remove(player.getUniqueId()), 3);
        });
      }
    });
  }

}
