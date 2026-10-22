package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnlyUpChallenge extends Setting {

  enum Verdict { KEEP, RAISE, FAIL }

  // The room sits on the first layer that is never bedrock
  private static final int FLOOR_ABOVE_MIN_HEIGHT = 5;
  private static final int START_LOGS = 8;

  public OnlyUpChallenge() {
    setUsesGlobalPunish(true);
  }

  static Verdict check(@Nullable Integer lowest, int y) {
    if (lowest == null || y > lowest) return Verdict.RAISE;
    return y < lowest ? Verdict.FAIL : Verdict.KEEP;
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bar, player) -> {
      Integer lowest = lowest(player, player.getWorld());
      bar.setTitle(getChallengeMessageKey("bossbar"), lowest == null ? "-" : lowest);
    });
    bossbar.show();
    if (ChallengeAPI.isStarted()) sendToStart();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void onTimerStart() {
    if (!isEnabled()) return;
    sendToStart();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (!getGameStateData().getBoolean("started") || !heights(event.getPlayer()).keys().isEmpty()) return;
    Location start = startRoom();
    if (start == null) return;
    event.getPlayer().teleport(start);
    setLowest(event.getPlayer(), start.getWorld(), start.getBlockY());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onRespawn(@NotNull PlayerRespawnEvent event) {
    if (!isEnabled()) return;
    heights(event.getPlayer()).remove(event.getRespawnLocation().getWorld().getName());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;
    // Jumps and falls only count where they end, standing, swimming or climbing
    if (!player.isOnGround() && !player.isInWater() && !player.isClimbing()) return;

    World world = event.getTo().getWorld();
    int y = event.getTo().getBlockY();
    Verdict verdict = check(lowest(player, world), y);
    if (verdict == Verdict.KEEP) return;
    if (verdict == Verdict.FAIL) {
      getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, player);
      ChallengeHelper.punish(player);
    }
    setLowest(player, world, y);
  }

  // Every player starts in the same small room at the bottom of the overworld, as in the original
  private void sendToStart() {
    if (getGameStateData().getBoolean("started")) return;
    Location start = startRoom();
    if (start == null) return;
    getGameStateData().set("started", true);
    broadcastFiltered(player -> {
      player.teleport(start);
      setLowest(player, start.getWorld(), start.getBlockY());
    });
  }

  @Nullable
  private Location startRoom() {
    World world = ChallengeAPI.getGameWorld(Environment.NORMAL);
    if (world == null) return null;
    Location spawn = world.getSpawnLocation();
    int floor = world.getMinHeight() + FLOOR_ABOVE_MIN_HEIGHT;
    Block center = world.getBlockAt(spawn.getBlockX(), floor + 1, spawn.getBlockZ());

    for (int x = -2; x <= 2; x++) {
      for (int z = -2; z <= 2; z++) {
        for (int y = -1; y <= 3; y++) {
          Block block = center.getRelative(x, y, z);
          boolean inside = Math.abs(x) <= 1 && Math.abs(z) <= 1 && y >= 0 && y <= 2;
          if (inside) {
            if (block.getType() != Material.CHEST) block.setType(Material.AIR);
          } else if (block.isPassable() || block.isLiquid()) {
            block.setType(Material.DEEPSLATE);
          }
        }
      }
    }

    Block chestBlock = center.getRelative(1, 0, 1);
    if (chestBlock.getType() != Material.CHEST) {
      chestBlock.setType(Material.CHEST);
      if (chestBlock.getState() instanceof Chest chest) chest.getBlockInventory().addItem(new ItemStack(Material.OAK_LOG, START_LOGS));
    }

    return center.getLocation().add(0.5, 0, 0.5);
  }

  @Nullable
  private Integer lowest(@NotNull Player player, @NotNull World world) {
    Document heights = heights(player);
    return heights.contains(world.getName()) ? heights.getInt(world.getName()) : null;
  }

  private void setLowest(@NotNull Player player, @NotNull World world, int y) {
    heights(player).set(world.getName(), y);
    bossbar.update(player);
  }

  @NotNull
  private Document heights(@NotNull Player player) {
    return getPlayerData(player.getUniqueId());
  }

}
