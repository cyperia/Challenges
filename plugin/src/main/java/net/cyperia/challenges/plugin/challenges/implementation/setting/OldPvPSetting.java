package net.cyperia.challenges.plugin.challenges.implementation.setting;

import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class OldPvPSetting extends Setting {

  public static final double DISABLED = 32, NORMAL = 4; // Values copied from BackToTheRoots

  @Override
  protected void onDisable() {
    broadcastAudience(player -> setAttackSpeed(player, NORMAL));
  }

  @Override
  protected void onEnable() {
    broadcastAudience(player -> setAttackSpeed(player, DISABLED));
  }

  @EventHandler
  public void onChangeWorldEvent(@NotNull PlayerChangedWorldEvent event) {
    if (!isEnabled()) return;
    setAttackSpeed(event.getPlayer(), DISABLED);
  }

  @EventHandler
  public void onJoin(@NotNull PlayerJoinEvent event) {
    if (!isEnabled()) return;
    setAttackSpeed(event.getPlayer(), DISABLED);
  }

  @EventHandler
  public void onQuit(@NotNull PlayerQuitEvent event) {
    setAttackSpeed(event.getPlayer(), NORMAL);
  }

  @EventHandler
  public void onSweepDamage(@NotNull EntityDamageEvent event) {
    if (!isEnabled()) return;
    if (event.getCause() != DamageCause.ENTITY_SWEEP_ATTACK) return;
    event.setCancelled(true);
  }

  protected void setAttackSpeed(@NotNull Player player, double value) {
    AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_SPEED);
    if (attribute == null) return;
    attribute.setBaseValue(value);
  }
}
