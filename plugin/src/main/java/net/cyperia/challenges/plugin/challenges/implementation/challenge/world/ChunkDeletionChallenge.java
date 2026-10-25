package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.bukkit.misc.ChunkFillQueue;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.collection.pair.Tuple;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkDeletionChallenge extends SettingModifier {

  private final ChunkFillQueue fills = new ChunkFillQueue();
  private final Map<Chunk, Tuple<io.papermc.paper.threadedregions.scheduler.ScheduledTask, Long>> chunks = new ConcurrentHashMap<>(); // access from concurrent threads

  public ChunkDeletionChallenge() {
    super(10, 120, 60);
    setValueUnit(ValueUnit.SECONDS);
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      MessageKey message = getChallengeMessageKey("bossbar");
      bossbar.setColor(BossBar.Color.PINK);
      if (!checkIfAllowed(player)) {
        Tuple<io.papermc.paper.threadedregions.scheduler.ScheduledTask, Long> taskTuple = chunks.get(player.getLocation().getChunk());
        if (taskTuple != null) {
          long remainingTime = calculateRemainingTime(taskTuple.getSecond());
          bossbar.setTitle(message, ChallengeAPI.formatTime(remainingTime));
        } else {
          bossbar.setTitle(message, ChallengeAPI.formatTime(getValue()));
        }
        return;
      }
      Chunk chunk = player.getLocation().getChunk();
      scheduleChunkDeletion(chunk);
      Long timeStamp = chunks.get(player.getLocation().getChunk()).getSecond();
      long remainingTime = calculateRemainingTime(timeStamp);

      bossbar.setTitle(message, ChallengeAPI.formatTime(remainingTime));
      bossbar.setProgress((float) remainingTime / getValue());
    });
    bossbar.show();
  }

  private long calculateRemainingTime(long timeStamp) {
    return (timeStamp - System.currentTimeMillis()) / 1000;
  }

  @Override
  protected void onDisable() {
    chunks.forEach((chunk, taskTuple) -> taskTuple.getFirst().cancel());
    chunks.clear();
    fills.clear();
    bossbar.hide();
  }

  @ScheduledTask(ticks = 1, async = false)
  public void clearChunks() {
    fills.drain();
  }

  @ScheduledTask(ticks = 20)
  public void onSecond() {
    if (!shouldExecuteEffect()) {
      return;
    }
    bossbar.update();
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (event.getTo() == null || !checkIfAllowed(event.getPlayer())) {
      return;
    }

    Chunk chunk = event.getTo().getChunk();
    scheduleChunkDeletion(chunk);
  }

  private boolean checkIfAllowed(@NotNull Player player) {
    return shouldExecuteEffect(player);
  }

  private void scheduleChunkDeletion(Chunk chunk) {
    chunks.computeIfAbsent(chunk, key -> {
      long timer = getValue() * 20L;
      Location center = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 0, chunk.getZ() * 16 + 8);
      io.papermc.paper.threadedregions.scheduler.ScheduledTask bukkitTask = Regions.later(plugin, center, () -> {
        if (!shouldExecuteEffect()) {
          io.papermc.paper.threadedregions.scheduler.ScheduledTask task = chunks.remove(chunk).getFirst();
          if (task != null) {
            task.cancel();
          }
          return;
        }
        io.papermc.paper.threadedregions.scheduler.ScheduledTask task = chunks.remove(chunk).getFirst();
        if (task != null) {
          task.cancel();
        }
        deleteChunk(chunk);
      }, timer);
      return new Tuple<>(bukkitTask, System.currentTimeMillis() + (timer * 50L));
    });
  }

  private void deleteChunk(@NotNull Chunk chunk) {
    World world = chunk.getWorld();
    // avoid end portal destruction. results in buggy ender dragon
    boolean keepBedrock = world.getEnvironment() == Environment.THE_END;

    fills.submit(chunk, world.getMinHeight(), world.getMaxHeight() - 1, Material.AIR,
      block -> !block.getType().isAir() && !(keepBedrock && block.getType() == Material.BEDROCK));
  }
}
