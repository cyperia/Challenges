package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkDeconstructionChallenge extends TimedChallenge {

  public ChunkDeconstructionChallenge() {
    super(1, 60, 20);
    setValueUnit(ValueUnit.SECONDS);
  }

  @Override
  protected void onTimeActivation() {
    if (ChallengeAPI.isWorldInUse()) return;

    Set<Chunk> chunks = ConcurrentHashMap.newKeySet();
    broadcastFiltered(player -> {
      Chunk chunk = player.getLocation().getChunk();
      if (!chunks.add(chunk) || !chunk.isLoaded()) return;
      deconstructChunk(chunk);
    });

    restartTimer();
  }

  private void deconstructChunk(@NotNull Chunk chunk) {
    Location center = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 0, chunk.getZ() * 16 + 8);

    for (int x = 0; x < 16; x++) {
      int finalX = x;
      Regions.nextTick(plugin, center, () -> {
        for (int z = 0; z < 16; z++) {
          deconstructAtLocation(chunk.getBlock(finalX, 0, z));
        }
      });
    }

  }

  private void deconstructAtLocation(@NotNull Block block) {
    Block lowestBreakableBlock = getLowestBreakableBlock(block);
    if (lowestBreakableBlock == null) return;

    lowestBreakableBlock.setType(Material.AIR, true);
  }

  @Nullable
  private Block getLowestBreakableBlock(@NotNull Block block) {
    Location location = block.getLocation();
    location.setY(block.getWorld().getMaxHeight());

    while (location.getY() >= block.getWorld().getMinHeight() && !isBreakable(location.getBlock())) {
      location.subtract(0, 1, 0);
    }

    Block currentBlock = location.getBlock();
    return currentBlock.getType() == Material.BEDROCK || currentBlock.isLiquid() ? null : location.getBlock();
  }

  private boolean isBreakable(@NotNull Block block) {
    return block.getType() != Material.BEDROCK && !block.getType().isAir() && !block.isLiquid();
  }

}
