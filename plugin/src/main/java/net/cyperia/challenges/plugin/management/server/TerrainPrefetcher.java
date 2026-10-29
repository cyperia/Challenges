package net.cyperia.challenges.plugin.management.server;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface TerrainPrefetcher {

  /**
   * Makes the terrain around {@code at} available before its first chunk load. Implementations must
   * not touch world state; the returned future may complete on any thread.
   */
  @NotNull
  CompletableFuture<Void> prefetch(@NotNull Location at);

}
