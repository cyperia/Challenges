package net.cyperia.challenges.plugin.challenges.implementation.challenge.damage;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DelayDamageChallenge extends TimedChallenge {

  private boolean canGetDamage = false;
  private final Map<Player, Double> damageMap = new HashMap<>();

  public DelayDamageChallenge() {
    super(1, 64, 4, false);
    setValueUnit(ValueUnit.SECONDS.scaled(30));
  }

  @Override
  protected void onTimeActivation() {

    final double totalDamage = damageMap.values().stream()
      .mapToDouble(Double::doubleValue)
      .sum();

    int playerCount = damageMap.size();

    if (playerCount > 0) {
      canGetDamage = true;
      broadcastFiltered(player -> {
        player.damage(totalDamage);
        MessageKey.of("extreme-force-battle-took-damage").send(player, Prefix.DAMAGE, totalDamage / 2);
      });
      canGetDamage = false;
    }

    damageMap.clear();
    restartTimer();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    if (canGetDamage) return;
    if (event.getCause() == EntityDamageEvent.DamageCause.VOID
      || event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;

    double damage = event.getFinalDamage();

    damageMap.put(player, damageMap.getOrDefault(player, 0.0) + damage);
    event.setCancelled(true);
  }
}
