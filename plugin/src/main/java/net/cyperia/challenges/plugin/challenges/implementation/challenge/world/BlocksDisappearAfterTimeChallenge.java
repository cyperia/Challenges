package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlocksDisappearAfterTimeChallenge extends SettingModifier {

  private final Map<Block, ScheduledTask> tasks = new ConcurrentHashMap<>();

  public BlocksDisappearAfterTimeChallenge() {
    super(60, 300);
    setValueUnit(ValueUnit.SECONDS);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull BlockPlaceEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    ScheduledTask oldTask = tasks.remove(event.getBlock());
    if (oldTask != null) oldTask.cancel();
    tasks.put(event.getBlock(), runTask(event.getBlock()));

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull BlockBreakEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    ScheduledTask oldTask = tasks.remove(event.getBlock());
    if (oldTask != null) oldTask.cancel();
  }

  private ScheduledTask runTask(@NotNull Block block) {
    return Regions.later(plugin, block.getLocation(), () -> block.setType(Material.AIR), getValue() * 20L);
  }

}
