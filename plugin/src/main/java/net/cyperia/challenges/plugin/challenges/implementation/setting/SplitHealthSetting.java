package net.cyperia.challenges.plugin.challenges.implementation.setting;

import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import org.bukkit.Bukkit;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SplitHealthSetting extends Setting {

  @Override
  protected void onEnable() {
    setHealth();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityRegainHealth(@NotNull EntityRegainHealthEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;

    Bukkit.getScheduler().runTaskLater(plugin, () -> setHealth(player, null), 1);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;

    Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> setHealth(player, event), 1);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    if (ignorePlayer(event.getPlayer())) return;
    setHealth();
  }

  /**
   * Calls {@link SplitHealthSetting#setHealth(Player, EntityDamageEvent)} for the player with the lowest health
   */
  private void setHealth() {
    Player player = null;

    for (Player currentPlayer : ChallengeHelper.getIngamePlayers()) {
      if (player == null || player.getHealth() > currentPlayer.getHealth()) {
        player = currentPlayer;
      }
    }
    if (player == null) return;
    setHealth(player, null);
  }

  public void setHealth(@NotNull Player player, @Nullable EntityDamageEvent damageEvent) {
    if (!shouldExecuteEffect()) return;

    AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) return;
    double health = Math.min(player.getHealth(), attribute.getValue());

    for (Player currentPlayer : ChallengeHelper.getIngamePlayers()) {
      if (currentPlayer.equals(player)) continue;

      if (health <= 0 && damageEvent != null) {
        applyLastDamageCause(currentPlayer, damageEvent);
      }

      currentPlayer.setHealth(health);
    }

  }

  @SuppressWarnings({"removal"})
  private void applyLastDamageCause(Player player, EntityDamageEvent damageEvent) {
    // marked for removal in api version 1.20.4 with no apparent replacement?
    player.setLastDamageCause(damageEvent);
  }

}
