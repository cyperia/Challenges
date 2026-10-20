package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.ForceTarget;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.commons.common.config.Document;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ForceBattleDisplayGoal<T extends ForceTarget<?>> extends ForceBattleGoal<T> {

  private Map<Player, ArmorStand> displayStands;

  @Override
  protected void onEnable() {
    displayStands = new HashMap<>();
    broadcastFiltered(this::updateDisplayStand);
    super.onEnable();
  }

  @Override
  protected void onDisable() {
    super.onDisable();
    displayStands.values().forEach(Entity::remove);
    displayStands = null;
  }

  public void updateDisplayStand(Player player) {
    ArmorStand armorStand = displayStands.computeIfAbsent(player, player1 -> {
      World world = player1.getWorld();
      ArmorStand entity = (ArmorStand) world
        .spawnEntity(player1.getLocation().clone().add(0, 2, 0), EntityType.ARMOR_STAND);
      entity.setVisible(false);
      entity.setInvulnerable(true);
      entity.setGravity(false);
      entity.setMarker(true);
      entity.setSilent(true);
      entity.setSmall(true);
      return entity;
    });
    armorStand.setVelocity(player.getVelocity().clone().multiply(2));
    armorStand.teleport(player.getLocation().clone().add(0, 2, 0));

    handleDisplayStandUpdate(player, armorStand);
  }

  public void handleDisplayStandUpdate(@NotNull Player player, @NotNull ArmorStand armorStand) {
    if (currentTarget.containsKey(player.getUniqueId())) {
      currentTarget.get(player.getUniqueId()).updateDisplayStand(armorStand);
    } else if (Objects.requireNonNull(armorStand.getEquipment()).getHelmet() != null) {
      armorStand.getEquipment().setHelmet(null);
    }
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    if (isEnabled()) {
      broadcastFiltered(this::updateDisplayStand);
    }
  }

  @Override
  public void setRandomTarget(Player player) {
    super.setRandomTarget(player);
    updateDisplayStand(player);
  }

  @EventHandler(ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    updateDisplayStand(event.getPlayer());
  }

  @EventHandler
  public void onStatusChange(PlayerIgnoreStatusChangeEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.isNotIgnored()) {
      setRandomTargetIfCurrentlyNone(event.getPlayer());
      updateDisplayStand(event.getPlayer());
    } else {
      ArmorStand stand = displayStands.remove(event.getPlayer());
      if (stand != null) {
        stand.remove();
      }
    }
  }

  @Override
  public void onTick() {
    super.onTick();
    for (Player player : displayStands.keySet()) {
      updateDisplayStand(player);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onQuit(PlayerQuitEvent event) {
    if (!shouldExecuteEffect()) return;
    ArmorStand stand = displayStands.remove(event.getPlayer());
    if (stand != null) {
      stand.remove();
    }
  }

}
