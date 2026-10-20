package net.cyperia.challenges.plugin.challenges.implementation.challenge.entities;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.impl.RandomMobAction;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.PlayerJumpEvent;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class NewEntityOnJumpChallenge extends Setting {

  @EventHandler(priority = EventPriority.HIGH)
  public void onJump(@NotNull PlayerJumpEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    spawnRandomEntity(event.getPlayer().getLocation());
  }

  private void spawnRandomEntity(@NotNull Location location) {
    if (location.getWorld() == null) return;
    EntityType type = globalRandom.choose(RandomMobAction.getSpawnableMobs());
    location.getWorld().spawnEntity(location, type);
  }

}
