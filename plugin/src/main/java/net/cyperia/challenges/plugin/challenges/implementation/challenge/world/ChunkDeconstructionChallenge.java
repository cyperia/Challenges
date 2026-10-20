package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.utils.misc.ListBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChunkDeconstructionChallenge extends TimedChallenge {

  public ChunkDeconstructionChallenge() {
    super(1, 60, 20);
    setValueUnit(ValueUnit.SECONDS);
  }

  @Override
  protected void onTimeActivation() {
    if (ChallengeAPI.isWorldInUse()) return;

    for (Chunk chunk : getChunksToDeconstruct()) {
      if (!chunk.isLoaded()) continue;
      deconstructChunk(chunk);
    }

    restartTimer();
  }

  private void deconstructChunk(@NotNull Chunk chunk) {

    for (int x = 0; x < 16; x++) {
      int finalX = x;
      Bukkit.getScheduler().runTask(plugin, () -> {
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

  private List<Chunk> getChunksToDeconstruct() {
    return new ListBuilder<Chunk>().fill(builder ->
      broadcastFiltered(player -> builder.addIfNotContains(player.getLocation().getChunk()))
    ).build();
  }

}
