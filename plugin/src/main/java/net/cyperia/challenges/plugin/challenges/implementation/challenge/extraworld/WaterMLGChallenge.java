package net.cyperia.challenges.plugin.challenges.implementation.challenge.extraworld;

import net.cyperia.challenges.plugin.challenges.implementation.setting.OneTeamLifeSetting;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.WorldDependentChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WaterMLGChallenge extends WorldDependentChallenge {

  public WaterMLGChallenge() {
    super(1, 10, 5);
    setValueUnit(ValueUnit.INTERVAL_MINUTES.jitter(10));
  }

  @Override
  public void startWorldChallenge() {
    Location currentLocation = new Location(getExtraWorld(), 0, 150, 0);

    teleportToWorld(false, (player, index) -> {
      currentLocation.add(100, 0, 0);
      player.getInventory().setHeldItemSlot(4);
      player.getInventory().setItem(4, new ItemStack(Material.WATER_BUCKET));
      player.teleport(currentLocation);
    });

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      teleportBack();
      restartTimer();
    }, 10 * 20);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerBucketEmpty(@NotNull PlayerBucketEmptyEvent event) {
    if (!isInExtraWorld()) return;

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      event.getBlock().setType(Material.AIR);
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        teleportBack(event.getPlayer());
      }, 40);
    }, 10);

  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onEntityDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!isInExtraWorld()) return;

    if (AbstractChallenge.getFirstInstance(OneTeamLifeSetting.class).isEnabled()) {
      teleportBack();
    } else {
      teleportBack(player);
    }
  }

}
