package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BlocksDisappearAfterTimeChallenge extends SettingModifier {

  private final Map<Block, BukkitTask> tasks = new HashMap<>();

  public BlocksDisappearAfterTimeChallenge() {
    super(60, 300);
    setValueUnit(ValueUnit.SECONDS);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull BlockPlaceEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    BukkitTask oldTask = tasks.remove(event.getBlock());
    if (oldTask != null) oldTask.cancel();
    tasks.put(event.getBlock(), runTask(event.getBlock()));

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull BlockBreakEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    BukkitTask oldTask = tasks.remove(event.getBlock());
    if (oldTask != null) oldTask.cancel();
  }

  private BukkitTask runTask(@NotNull Block block) {
    return Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), getValue() * 20L);
  }

}
