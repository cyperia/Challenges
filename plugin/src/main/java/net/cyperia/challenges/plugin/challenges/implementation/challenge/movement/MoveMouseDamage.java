package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoveMouseDamage extends SettingModifier {

  private final Map<UUID, Map.Entry<Float, Float>> lastView = new HashMap<>();

  public MoveMouseDamage() {
    super(60);
    setValueUnit(ValueUnit.DAMAGE);
  }

  @ScheduledTask(ticks = 1, timerPolicy = TimerPolicy.ALWAYS)
  public void onTick() {
    if (!shouldExecuteEffect()) {
      lastView.clear();
      return;
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      if (ignorePlayer(player)) {
        lastView.remove(player.getUniqueId());
        continue;
      }
      if (player.getNoDamageTicks() > 0) continue;

      float yaw = player.getLocation().getYaw();
      float pitch = player.getLocation().getPitch();
      Map.Entry<Float, Float> pair = lastView.getOrDefault(player.getUniqueId(), new SimpleEntry<>(yaw, pitch));
      lastView.put(player.getUniqueId(), pair);

      if (yaw != pair.getKey() || pitch != pair.getValue()) {
        Bukkit.getScheduler().runTask(plugin, () -> {
          if (player.getNoDamageTicks() > 0) return;
          getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, player);
          player.damage(getValue());
          player.setNoDamageTicks(5);
          Bukkit.getScheduler().runTaskLater(plugin, () -> lastView.remove(player.getUniqueId()), 3);
        });
      }
    }
  }

}
