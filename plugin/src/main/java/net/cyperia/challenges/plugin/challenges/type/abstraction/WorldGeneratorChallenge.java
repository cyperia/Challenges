package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.server.GameWorldStorage;
import net.cyperia.challenges.plugin.spigot.generator.ConvertingWorldGenerator;
import net.cyperia.challenges.plugin.spigot.generator.VoidMapGenerator;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.AsyncStructureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class WorldGeneratorChallenge extends OneEnabledSetting {

  // ServerLevel.END_SPAWN_POINT, where an end portal drops travellers, facing Direction.WEST
  private static final int END_SPAWN_X = 100, END_SPAWN_Y = 50, END_SPAWN_Z = 0;
  private static final float END_SPAWN_YAW = 90;

  public WorldGeneratorChallenge() {
    super("world-generator");
  }

  /**
   * Null keeps the server's own world of that environment.
   */
  @Nullable
  protected abstract ChunkGenerator createGenerator(@NotNull Environment environment);

  /**
   * Only has an effect for generators that let vanilla structures generate.
   */
  protected boolean generatesStructure(@NotNull Environment environment, @NotNull Structure structure) {
    return true;
  }

  @Override
  protected void onEnable() {
    ChallengeHelper.runSync(() -> {
      if (isEnabled()) getStorage().useGeneratedWorlds(getWorldNamePrefix(), this::createGenerator);
    });
  }

  @Override
  protected void onDisable() {
    ChallengeHelper.runSync(() -> {
      if (!isEnabled()) getStorage().useServerWorlds();
    });
  }

  // Deliberately skips onDisable: players stay in the generated worlds, which the loaded setting puts back into use on the next start
  @Override
  public void handleShutdown() {
  }

  @EventHandler
  public void onStructureSpawn(@NotNull AsyncStructureSpawnEvent event) {
    if (isGeneratedWorld(event.getWorld()) && !generatesStructure(event.getWorld().getEnvironment(), event.getStructure())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onStructureKept(@NotNull AsyncStructureSpawnEvent event) {
    World world = event.getWorld();
    if (isGeneratedWorld(world) && world.getGenerator() instanceof VoidMapGenerator generator) {
      generator.addStructure(world, event.getStructure(), event.getBoundingBox());
    }
  }

  @EventHandler
  public void onChunkLoad(@NotNull ChunkLoadEvent event) {
    World world = event.getWorld();
    if (!isGeneratedWorld(world)) return;
    if (world.getGenerator() instanceof VoidMapGenerator generator) generator.cleanUp(event.getChunk());
    if (world.getGenerator() instanceof ConvertingWorldGenerator generator) generator.cleanUp(event.getChunk(), event.isNewChunk());
  }

  private boolean isGeneratedWorld(@NotNull World world) {
    return world.getName().equals(GameWorldStorage.getGeneratedWorldName(getWorldNamePrefix(), world.getEnvironment()));
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    if (!isEnabled()) return;
    Player player = event.getPlayer();
    World replacement = getStorage().getReplacement(player.getWorld());
    if (replacement != null) player.teleport(BlockUtils.getCenteredSpawn(replacement));
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onRespawn(@NotNull PlayerRespawnEvent event) {
    if (!isEnabled()) return;
    World replacement = getStorage().getReplacement(event.getRespawnLocation().getWorld());
    if (replacement != null) event.setRespawnLocation(BlockUtils.getCenteredSpawn(replacement));
  }

  @EventHandler
  public void onPlayerPortal(@NotNull PlayerPortalEvent event) {
    if (!isEnabled()) return;
    Location to = event.getTo();
    World replacement = getReplacement(to);
    if (replacement != null) event.setTo(redirectPortalTravel(to, replacement, event.getCause() == TeleportCause.END_PORTAL, true));
  }

  @EventHandler
  public void onEntityPortal(@NotNull EntityPortalEvent event) {
    if (!isEnabled()) return;
    Location to = event.getTo();
    World replacement = getReplacement(to);
    if (replacement == null) return;

    // The event carries no cause, but only an end portal leads into or out of the end
    boolean endPortal = replacement.getEnvironment() == Environment.THE_END
      || event.getFrom().getWorld().getEnvironment() == Environment.THE_END;
    event.setTo(redirectPortalTravel(to, replacement, endPortal, false));
  }

  // The server resolves every portal against its own worlds, so a destination in a replaced one has to be moved over
  @Nullable
  private static World getReplacement(@Nullable Location to) {
    if (to == null || to.getWorld() == null) return null;
    return getStorage().getReplacement(to.getWorld());
  }

  @NotNull
  static Location redirectPortalTravel(@NotNull Location to, @NotNull World replacement, boolean endPortal, boolean player) {
    if (!endPortal) {
      // The coordinates are already scaled, and the exit portal is searched for in whatever world the event ends up in
      Location redirected = to.clone();
      redirected.setWorld(replacement);
      return redirected;
    }

    // Entering the end, the server builds its spawn platform in its own end only
    return replacement.getEnvironment() == Environment.THE_END
      ? createEndSpawnPlatform(replacement, player)
      : BlockUtils.getCenteredSpawn(replacement);
  }

  // Mirrors EndPlatformFeature.createEndPlatform; players land on the floor, other entities one block above it
  @NotNull
  private static Location createEndSpawnPlatform(@NotNull World world, boolean player) {
    int floorY = END_SPAWN_Y - 2;
    for (int x = END_SPAWN_X - 2; x <= END_SPAWN_X + 2; x++) {
      for (int z = END_SPAWN_Z - 2; z <= END_SPAWN_Z + 2; z++) {
        for (int y = floorY; y <= END_SPAWN_Y + 1; y++) {
          Material material = y == floorY ? Material.OBSIDIAN : Material.AIR;
          Block block = world.getBlockAt(x, y, z);
          if (block.getType() != material) block.setType(material, false);
        }
      }
    }
    return new Location(world, END_SPAWN_X + 0.5, player ? END_SPAWN_Y - 1 : END_SPAWN_Y, END_SPAWN_Z + 0.5, END_SPAWN_YAW, 0);
  }

  @NotNull
  private String getWorldNamePrefix() {
    return "challenges-" + getUniqueName();
  }

  @NotNull
  private static GameWorldStorage getStorage() {
    return Challenges.getInstance().getGameWorldStorage();
  }

}
