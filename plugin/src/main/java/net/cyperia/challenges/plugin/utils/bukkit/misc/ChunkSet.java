package net.cyperia.challenges.plugin.utils.bukkit.misc;

import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.GsonDocument;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A set of chunks that can be written into the game state. Keys are kept per world, because a chunk key
 * only identifies a chunk within its own world.
 */
public final class ChunkSet {

  private final Map<String, Set<Long>> chunks = new HashMap<>();

  /**
   * Adds the chunk and answers whether it was not in the set yet.
   */
  public boolean add(@NotNull Chunk chunk) {
    return chunksIn(chunk.getWorld().getName()).add(chunk.getChunkKey());
  }

  public boolean contains(@NotNull Chunk chunk) {
    return chunksIn(chunk.getWorld().getName()).contains(chunk.getChunkKey());
  }

  public void remove(@NotNull String world, long chunkKey) {
    chunksIn(world).remove(chunkKey);
  }

  public void clear() {
    chunks.clear();
  }

  public void writeGameState(@NotNull Document document, @NotNull String key) {
    Document written = new GsonDocument();
    chunks.forEach((world, keys) -> written.set(world, keys.stream().map(String::valueOf).toList()));
    document.set(key, written);
  }

  public void loadGameState(@NotNull Document document, @NotNull String key) {
    chunks.clear();
    Document stored = document.getDocument(key);

    // Read by the names the document holds, not by the worlds that exist right now: a game world can be
    // created after the game state is loaded, and its chunks would be dropped on the next save.
    for (String world : stored.keys()) {
      Set<Long> keys = new HashSet<>();

      for (String chunkKey : stored.getStringList(world)) {
        try {
          keys.add(Long.parseLong(chunkKey));
        } catch (NumberFormatException exception) {
          Logger.error("Invalid chunk key in {}: {}, {}", key, world, chunkKey);
        }
      }

      if (!keys.isEmpty()) chunks.put(world, keys);
    }
  }

  @NotNull
  private Set<Long> chunksIn(@NotNull String world) {
    return chunks.computeIfAbsent(world, name -> new HashSet<>());
  }

}
