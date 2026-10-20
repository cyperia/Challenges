package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.EntityDamageByPlayerEvent;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobTransformationChallenge extends Setting {

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      bossbar.setColor(BossBar.Color.GREEN);
      EntityType type = getPlayerData(player).getEnum("type", EntityType.class);
      Object typeName = type == null ? "None" : type;
      bossbar.setTitle(getChallengeMessageKey("bossbar"), typeName);
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityDamageByPlayer(@NotNull EntityDamageByPlayerEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getEntity() instanceof Player || !(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof EnderDragon)
      return;

    Player player = event.getDamager();
    if (ignorePlayer(player)) return;

    EntityType type = getPlayerData(player).getEnum("type", event.getEntityType());

    if (type != event.getEntityType()) {
      event.getEntity().remove();
      event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), type);
    }
    getPlayerData(player).set("type", event.getEntityType());
    bossbar.update(player);
  }

  private EntityType getType(@NotNull Player player, @Nullable EntityType defaultType) {
    EntityType type = getPlayerData(player).getEnum("type", EntityType.class);
    if (type == null) return defaultType;
    return type;
  }

}
