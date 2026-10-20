package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuSetting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnvilRainChallenge extends MenuSetting {

  private final Random random = new Random();
  int currentTime = 0;

  private final NumberSubSetting timeSetting;
  private final NumberSubSetting countSetting;
  private final NumberSubSetting rangeSetting;
  private final NumberSubSetting damageSetting;

  public AnvilRainChallenge() {
    timeSetting = registerSetting("time", new NumberSubSetting(
      new ItemStack(Material.CLOCK), getSubSettingSpace("time"),
      1, 30, 7
    ).unit(ValueUnit.INTERVAL_SECONDS));
    countSetting = registerSetting("count", new NumberSubSetting(
      new ItemStack(Material.FLINT), getSubSettingSpace("count"),
      1, 30, 8
    ));
    rangeSetting = registerSetting("range", new NumberSubSetting(
      new ItemStack(Material.COMPASS), getSubSettingSpace("range"),
      1, 3, 2
    ).unit(ValueUnit.CHUNKS));
    damageSetting = registerSetting("damage", new NumberSubSetting(
      new ItemStack(Material.IRON_SWORD), getSubSettingSpace("damage"),
      1, 60, 30
    ).unit(ValueUnit.DAMAGE));
  }

  @Override
  protected void onDisable() {
    removeAnvils();
  }

  @TimerTask(async = false, status = TimerStatus.PAUSED)
  public void onPause() {
    removeAnvils();
  }

  private void removeAnvils() {
    for (World world : Bukkit.getWorlds()) {
      for (FallingBlock entity : world.getEntitiesByClass(FallingBlock.class)) {
        // TODO: SWITCH CASE
        String name = entity.getBlockData().getMaterial().name();
        if (!name.contains("ANVIL")) continue;
        entity.remove();
      }
    }
  }

  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    currentTime++;

    if (currentTime >= getTime()) {
      currentTime = 0;
      handleTimeActivation();
    }

  }

  private void handleTimeActivation() {
    List<Chunk> chunks = new ArrayList<>();
    for (Player player : ChallengeHelper.getIngamePlayers()) {
      List<Chunk> targetChunks = getTargetChunks(player.getLocation().getChunk());
      for (Chunk targetChunk : targetChunks) {
        if (chunks.contains(targetChunk)) continue;
        chunks.add(targetChunk);
        spawnAnvils(targetChunk, getAnvilHeight(player.getLocation().getBlockY()));
      }
    }

  }

  private void spawnAnvils(@NotNull Chunk chunk, int height) {
    for (int i = 0; i < getCount(); i++) {
      Block block = getRandomBlockInChunk(chunk, height);
      Location location = block.getLocation().add(0.5, 0, 0.5);
      block.getWorld().spawnFallingBlock(location, Material.ANVIL, ((byte) 0));
    }
  }

  private List<Chunk> getTargetChunks(@NotNull Chunk origin) {
    List<Chunk> chunks = new ArrayList<>();

    int originX = origin.getX();
    int originZ = origin.getZ();

    int range = getRange();
    for (int x = -range; x <= range; x++) {
      for (int z = -range; z <= range; z++) {
        Chunk chunkAt = origin.getWorld().getChunkAt(originX + x, originZ + z);
        chunks.add(chunkAt);
      }

    }

    return chunks;
  }

  private Block getRandomBlockInChunk(@NotNull Chunk chunk, int y) {
    int x = random.nextInt(16);
    int z = random.nextInt(16);
    return chunk.getBlock(x, y, z);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityChangeBlock(@NotNull EntityChangeBlockEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getEntity() instanceof FallingBlock)) return;

    String name = ((FallingBlock) event.getEntity()).getBlockData().getMaterial().name();
    if (!name.contains("ANVIL")) return;

    Block block = event.getBlock().getLocation().subtract(0, 1, 0).getBlock();
    if (block.getType().isAir()) return;

    event.getBlock().setType(Material.AIR);
    event.setCancelled(true);
    event.getEntity().remove();
    destroyRandomBlocks(event.getBlock().getLocation());
    applyDamageToNearEntities(event.getBlock().getLocation().add(0.5, 0.5, 0.5));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDrop(@NotNull EntityDropItemEvent event) {
    if (event.getItemDrop().getItemStack().getType() != Material.ANVIL) return;
    if (event.getEntityType() != EntityType.FALLING_BLOCK) return;
    String name = ((FallingBlock) event.getEntity()).getBlockData().getMaterial().name();
    if (!name.contains("ANVIL")) return;

    event.setCancelled(true);

    destroyRandomBlocks(event.getEntity().getLocation());
    applyDamageToNearEntities(event.getEntity().getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5));
  }

  public void destroyRandomBlocks(@NotNull Location origin) {
    int i = random.nextInt(2);

    if (i == 0) return;
    int blocks = getCount() < 16 ? 0 : 1;
    while (blocks < 2 && origin.getBlockY() > 1) {

      if (origin.getBlock().getType() == Material.WATER || origin.getBlock().getType() == Material.LAVA)
        return;
      origin.subtract(0, 1, 0);
      if (origin.getBlock().isPassable()) blocks--;

      origin.getBlock().setType(Material.AIR);
      blocks++;
    }

  }

  public void applyDamageToNearEntities(@NotNull Location location) {
    if (location.getWorld() == null) return;
    for (Entity entity : location.getWorld().getNearbyEntities(location, 0.25, 0.25, 0.25)) {
      if (!(entity instanceof LivingEntity livingEntity)) continue;
      livingEntity.damage(getDamage());
    }

  }

  private int getTime() {
    return timeSetting.getAsInt();
  }

  private int getRange() {
    return rangeSetting.getAsInt();
  }

  private int getCount() {
    return countSetting.getAsInt();
  }

  private int getDamage() {
    return damageSetting.getAsInt();
  }

  private int getAnvilHeight(int currentHeight) {
    return currentHeight + 50;
  }

}
