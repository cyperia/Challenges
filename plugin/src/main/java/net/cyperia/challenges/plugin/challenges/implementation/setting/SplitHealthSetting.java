package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Everybody shares one health value. It is owned by the global region: a player whose health changed
 * reports it there, where the change is added to the shared value, so damage that two regions deal at
 * the same time adds up instead of one overwriting the other. Each player is then set to the shared value
 * on their own thread.
 */
public class SplitHealthSetting extends Setting {

  private static final double EPSILON = 1e-6;

  // Written on the global region only
  private volatile double shared = Double.NaN;
  private volatile EntityDamageEvent lethalDamage;
  // The health each player had when last merged or set; a player's thread compares against it to notice
  // a change that was not reported yet
  private final Map<UUID, Double> known = new ConcurrentHashMap<>();

  @Override
  protected void onEnable() {
    Regions.global(plugin, this::initialize);
  }

  @Override
  protected void onDisable() {
    known.clear();
    shared = Double.NaN;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;

    report(player, null);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;

    report(player, event);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    if (ignorePlayer(event.getPlayer())) return;
    Regions.global(plugin, this::initialize);
  }

  // The health after the change is read a tick later, once the server applied it
  private void report(@NotNull Player player, @Nullable EntityDamageEvent damage) {
    Regions.later(plugin, player, () -> {
      double health = player.getHealth();
      Regions.global(plugin, () -> merge(player, health, damage));
    }, 1);
  }

  // The lowest health of everybody becomes the shared one
  private void initialize() {
    if (!shouldExecuteEffect()) return;

    double lowest = Double.MAX_VALUE;
    for (Player player : ChallengeHelper.getIngamePlayers()) lowest = Math.min(lowest, player.getHealth());
    if (lowest == Double.MAX_VALUE) return;

    shared = lowest;
    lethalDamage = null;
    known.clear();
    applyToAll();
  }

  private void merge(@NotNull Player player, double health, @Nullable EntityDamageEvent damage) {
    if (!shouldExecuteEffect()) return;
    if (Double.isNaN(shared)) {
      initialize();
      return;
    }

    double before = known.getOrDefault(player.getUniqueId(), shared);
    shared = Math.max(0, shared + health - before);
    known.put(player.getUniqueId(), health);
    if (shared <= 0 && damage != null) lethalDamage = damage;
    applyToAll();
  }

  private void applyToAll() {
    plugin.getPlayerManager().forEachChallengePlayer(this::apply);
  }

  // Runs on the player's thread
  private void apply(@NotNull Player player) {
    double current = player.getHealth();
    Double last = known.get(player.getUniqueId());
    if (last != null && Math.abs(current - last) > EPSILON) {
      // Changed since the last merge and not reported yet: merging it re-applies the sum afterwards
      Regions.global(plugin, () -> merge(player, current, null));
      return;
    }

    AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) return;
    double target = Math.min(shared, attribute.getValue());
    if (Math.abs(current - target) > EPSILON) {
      EntityDamageEvent lethal = lethalDamage;
      if (target <= 0 && lethal != null) applyLastDamageCause(player, lethal);
      player.setHealth(target);
    }
    known.put(player.getUniqueId(), target);
  }

  @SuppressWarnings({"removal"})
  private void applyLastDamageCause(Player player, EntityDamageEvent damageEvent) {
    // marked for removal in api version 1.20.4 with no apparent replacement?
    player.setLastDamageCause(damageEvent);
  }

}
