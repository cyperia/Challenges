package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FloorIsLavaChallenge extends SettingModifier {

  private final Map<UUID, Block> standingOn = new HashMap<>();

  public FloorIsLavaChallenge() {
    super(1, 60, 30);
    setValueUnit(ValueUnit.SECONDS);
  }

  @Override
  protected void onDisable() {
    super.onDisable();
    standingOn.clear();
  }

  // Checked every tick as well as on every move: a teleport fires no move, and the landing between two
  // jumps can last a single tick that the moves of a lagging client arrive around.
  @ScheduledTask(ticks = 1, async = false)
  public void checkGround() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      step(player, player.getLocation());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect() || !event.hasChangedPosition()) return;
    step(event.getPlayer(), event.getTo());
  }

  @EventHandler
  public void onQuit(@NotNull PlayerQuitEvent event) {
    standingOn.remove(event.getPlayer().getUniqueId());
  }

  private void step(@NotNull Player player, @NotNull Location at) {
    if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

    Block ground = BlockUtils.getSolidGround(player, at);
    if (ground == null || ground.equals(standingOn.put(player.getUniqueId(), ground))) return;

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      BlockUtils.setBlockNatural(ground, Material.MAGMA_BLOCK, true);
      Bukkit.getScheduler().runTaskLater(plugin, () -> BlockUtils.setBlockNatural(ground, Material.LAVA, true), getValue() * 20L);
    }, getValue() * 20L);
  }

}
