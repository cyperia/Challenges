package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OneTeamLifeSetting extends Setting {

  // The players this setting is killing, whose deaths must not start another round of kills
  private final Set<UUID> killing = ConcurrentHashMap.newKeySet();

  public OneTeamLifeSetting() {
    super(true);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDeath(@NotNull PlayerDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    if (killing.contains(event.getEntity().getUniqueId())) return;

    Player dead = event.getEntity();
    Regions.nextTick(plugin, () -> killOthers(dead));
  }

  // Runs on the global region. The kills happen on the threads of the players, so the round is only
  // over once every one of them reported back.
  private void killOthers(@NotNull Player dead) {
    DeathMessageSetting messages = AbstractChallenge.getFirstInstance(DeathMessageSetting.class);
    List<Player> victims = ChallengeHelper.getIngamePlayers().stream().filter(player -> player != dead).toList();
    List<UUID> ids = victims.stream().map(Player::getUniqueId).toList();
    if (victims.isEmpty()) {
      AbstractChallenge.getFirstInstance(RespawnSetting.class).checkAllPlayersDead();
      return;
    }

    killing.addAll(ids);
    messages.hideMessagesOf(ids);
    AtomicInteger remaining = new AtomicInteger(victims.size());
    Runnable killed = () -> {
      if (remaining.decrementAndGet() != 0) return;
      Regions.global(plugin, () -> {
        killing.removeAll(ids);
        messages.showMessagesOf(ids);
        AbstractChallenge.getFirstInstance(RespawnSetting.class).checkAllPlayersDead();
      });
    };
    for (Player victim : victims) ChallengeHelper.kill(victim, killed);
  }

}
