package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.utils.bukkit.misc.ChunkFillQueue;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class TsunamiChallenge extends TimedChallenge {

  public static final int RANGE = 5;

  private final ChunkFillQueue fills = new ChunkFillQueue();
  private final Set<Chunk> floodedChunks = new HashSet<>();

  private int waterHeight = Integer.MAX_VALUE, lavaHeight = 0;

  public TsunamiChallenge() {
    super(1, 40, 4);
    setValueUnit(ValueUnit.SECONDS.scaled(15));
  }

  @Override
  protected void onEnable() {
    if (waterHeight == Integer.MAX_VALUE) {
      waterHeight = ChallengeAPI.getGameWorld(Environment.NORMAL).getMinHeight();
    }
    bossbar.setContent((bossbar, player) -> {
      World world = player.getWorld();
      Environment environment = world.getEnvironment();
      int height = environment == Environment.NORMAL ? waterHeight : lavaHeight;
      if (height < (world.getMaxHeight() - 1))
        bossbar.setProgress(getProgress());

      if (environment == Environment.NORMAL) {
        bossbar.setColor(BossBar.Color.BLUE);
        bossbar.setTitle(getChallengeMessageKey("bossbar-water"), waterHeight);
      } else if (environment == Environment.NETHER) {
        bossbar.setColor(BossBar.Color.RED);
        bossbar.setTitle(getChallengeMessageKey("bossbar-lava"), lavaHeight);
      } else {
        bossbar.setVisible(false);
      }
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    fills.clear();
    bossbar.hide();
  }

  @Override
  protected void handleCountdown() {
    bossbar.update();
  }

  @ScheduledTask(ticks = 1, async = false)
  public void fillFlood() {
    fills.drain();
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();

    for (World world : ChallengeAPI.getGameWorlds()) {
      if (ChallengeAPI.isPlayerInGameWorld(world)) {
        if (world.getEnvironment() == Environment.NORMAL) {
          if (waterHeight >= (world.getMaxHeight() - 1)) continue;
          waterHeight++;
        } else if (world.getEnvironment() == Environment.NETHER) {
          if (lavaHeight >= (world.getMaxHeight() - 1)) continue;
          lavaHeight++;
        }
      }
    }

    Set<Chunk> previouslyFlooded = new HashSet<>(floodedChunks);
    bossbar.update();
    floodedChunks.clear();
    for (Chunk chunk : getChunksToFlood()) {
      floodChunk(chunk, !previouslyFlooded.contains(chunk));
    }

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;

    Chunk newChunk = event.getTo().getChunk();
    if (event.getFrom().getChunk() == newChunk) return;

    for (Chunk chunk : getChunksAroundChunk(newChunk)) {
      floodChunk(chunk, true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTeleport(@NotNull PlayerTeleportEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;

    Chunk newChunk = event.getTo().getChunk();
    if (event.getFrom().getChunk() == newChunk) return;

    bossbar.update();

    for (Chunk chunk : getChunksAroundChunk(newChunk)) {
      floodChunk(chunk, true);
    }
  }

  @EventHandler
  public void onChunkUnload(@NotNull ChunkUnloadEvent event) {
    floodedChunks.remove(event.getChunk());
    fills.forget(event.getChunk());
  }

  private void floodChunk(@NotNull Chunk chunk, boolean discovered) {
    if (!floodedChunks.add(chunk)) return;

    World world = chunk.getWorld();
    if (world.getEnvironment() == Environment.THE_END) return;

    boolean overworld = world.getEnvironment() == Environment.NORMAL;
    int height = overworld ? waterHeight : lavaHeight;
    int fromY = world.getMinHeight() + 2;
    // A chunk that was already flooded only misses the layer the tide just gained.
    if (!discovered) fromY = Math.max(fromY, height);

    fills.submit(chunk, fromY, height, overworld ? Material.WATER : Material.LAVA,
      block -> isFloodable(block, overworld));
  }

  // Passable blocks flood; water additionally swallows the lava it runs into.
  private boolean isFloodable(@NotNull Block block, boolean overworld) {
    Material type = block.getType();
    if (overworld && type == Material.LAVA) return true;
    return type != Material.WATER && type != Material.LAVA && block.isPassable();
  }

  @NotNull
  private Set<Chunk> getChunksToFlood() {
    Set<Chunk> chunks = new LinkedHashSet<>();
    broadcastFiltered(player -> chunks.addAll(getChunksAroundChunk(player.getLocation().getChunk())));
    return chunks;
  }

  @NotNull
  private Set<Chunk> getChunksAroundChunk(@NotNull Chunk origin) {
    World world = origin.getWorld();
    Set<Chunk> chunks = new LinkedHashSet<>();

    for (int x = -RANGE; x <= RANGE; x++) {
      for (int z = -RANGE; z <= RANGE; z++) {
        int chunkX = origin.getX() + x;
        int chunkZ = origin.getZ() + z;
        // getChunkAt generates missing chunks, which is 121 chunk generations per chunk crossing.
        if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
        chunks.add(world.getChunkAt(chunkX, chunkZ));
      }
    }

    return chunks;
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);
    document.set("waterHeight", waterHeight);
    document.set("lavaHeight", lavaHeight);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    waterHeight = document.getInt("waterHeight");
    lavaHeight = document.getInt("lavaHeight");
  }

}
