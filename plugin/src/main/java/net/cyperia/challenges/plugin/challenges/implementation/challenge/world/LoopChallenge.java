package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.policy.PlayerCountPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public class LoopChallenge extends Setting {

  private static final Map<Loop, Long> loops = new HashMap<>();

  @ScheduledTask(ticks = 1, async = false)
  public void onTick() {
    long currentTimeMillis = System.currentTimeMillis();

    for (Map.Entry<Loop, Long> entry : new ArrayList<>(loops.entrySet())) {

      if (entry.getValue() + 1000 <= currentTimeMillis) {
        entry.getKey().execute();
        loops.put(entry.getKey(), currentTimeMillis);
      }

    }

  }

  @TimerTask(status = TimerStatus.PAUSED, async = false, playerPolicy = PlayerCountPolicy.ALWAYS)
  public void onPause() {
    clearLoops();
  }

  private void clearLoops() {
    if (loops.isEmpty()) return;
    broadcastAudience(player -> new SoundSample().addSound(Sound.ENTITY_ITEM_BREAK, 0.5f).play(player));
    getChallengeMessageKey("cleared").broadcast(Prefix.CHALLENGES, loops.size());
    loops.clear();
  }

  private void createLoop(@NotNull Loop loop) {
    loops.put(loop, System.currentTimeMillis());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onSneak(@NotNull PlayerToggleSneakEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (!event.isSneaking()) return;
    clearLoops();
  }


  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onProjectileLaunch(@NotNull ProjectileLaunchEvent event) {
    if (!shouldExecuteEffect()) return;
    ProjectileSource shooter = event.getEntity().getShooter();
    if (shooter == null) return;
    if (shooter instanceof Player && ignorePlayer(((Player) shooter))) return;
    createLoop(new ProjectileLaunchLoop(event));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;
    double damage = event.getFinalDamage() + event.getDamage(DamageModifier.ABSORPTION);
    if (damage == 0) return;
    createLoop(new EntityDamageLoop(((LivingEntity) event.getEntity()), event.getCause(), damage));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull BlockPlaceEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    createLoop(new BlockPlaceLoop(event.getBlock().getType(), event.getPlayer(), event.getBlockAgainst().getFace(event.getBlock()), event.getBlock()));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(@NotNull BlockBreakEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;

    RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getLocation().getDirection(), 5);
    if (result == null) return;
    if (result.getHitBlockFace() == null) return;


    createLoop(new BlockBreakLoop(player.getInventory().getItemInMainHand(), player, result.getHitBlockFace().getOppositeFace(), event.getBlock()));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull PlayerDropItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    createLoop(new DropLoop(event.getItemDrop().getItemStack(), event.getPlayer()));
  }

  private interface Loop {

    void execute();

    default void cancel() {
      loops.remove(this);
    }

  }

  private static class ProjectileLaunchLoop implements Loop {

    private final Vector launchVelocity;
    private final Location launchLocation;
    private final EntityType entityType;

    public ProjectileLaunchLoop(ProjectileLaunchEvent event) {
      this.launchVelocity = event.getEntity().getVelocity();
      this.launchLocation = event.getLocation();
      this.entityType = event.getEntityType();
    }

    @Override
    public void execute() {
      if (launchLocation.getWorld() == null) return;
      Projectile projectile = (Projectile) launchLocation.getWorld().spawnEntity(launchLocation, entityType);
      projectile.setVelocity(launchVelocity);
      if (projectile instanceof Arrow) {
        ((Arrow) projectile).setPickupStatus(PickupStatus.DISALLOWED);
      }
    }

  }

  private static class EntityDamageLoop implements Loop {

    private final LivingEntity entity;
    private final double damage;

    public EntityDamageLoop(LivingEntity entity, DamageCause cause, double damage) {
      this.entity = entity;
      this.damage = damage;
    }

    @Override
    public void execute() {
      if (entity.isDead()) {
        cancel();
        return;
      }
      if (entity instanceof Player && ignorePlayer(((Player) entity))) return;
      entity.damage(damage);
    }
  }

  private static class BlockPlaceLoop implements Loop {

    private final Material material;
    private final Player player;
    private final BlockFace blockFace;
    private Block currentBlock;

    public BlockPlaceLoop(Material material, Player player, BlockFace blockFace, Block currentBlock) {
      this.material = material;
      this.player = player;
      this.blockFace = blockFace;
      this.currentBlock = currentBlock;
    }

    @Override
    public void execute() {
      if (ignorePlayer(player)) return;

      currentBlock = currentBlock.getRelative(blockFace);

      if (currentBlock.getY() > currentBlock.getWorld().getMaxHeight() || currentBlock.getY() < currentBlock.getWorld().getMinHeight()) {
        cancel();
        return;
      } else if (!currentBlock.getType().isAir() && currentBlock.getType().isSolid()) {
        return;
      } else if (!decreaseMaterial()) {
        cancel();
        return;
      }

      currentBlock.setType(material);
      RepeatInChunkChallenge.repeatInEveryChunk(currentBlock);
    }

    private boolean decreaseMaterial() {
      ItemStack item = Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(itemStack -> itemStack.getType() == material).findFirst().orElse(null);
      if (item == null) return false;
      item.setAmount(item.getAmount() - 1);
      return true;
    }

  }

  private static class BlockBreakLoop implements Loop {

    private final Player player;
    private final BlockFace blockFace;
    private ItemStack itemStack;
    private Block currentBlock;

    public BlockBreakLoop(ItemStack itemStack, Player player, BlockFace blockFace, Block currentBlock) {
      this.itemStack = itemStack;
      this.player = player;
      this.blockFace = blockFace;
      this.currentBlock = currentBlock;
    }

    @Override
    public void execute() {
      if (ignorePlayer(player)) return;

      currentBlock = currentBlock.getRelative(blockFace);

      if (currentBlock.getY() > currentBlock.getWorld().getMaxHeight() || currentBlock.getY() < currentBlock.getWorld().getMinHeight()) {
        cancel();
        return;
      } else if (currentBlock.getType().isAir()) {
        return;
      } else if (!decreaseDurability()) {
        cancel();
        return;
      }

      if (currentBlock.getType() == Material.BEDROCK) return;

      Material before = currentBlock.getType();
      if (cantBeBroken(currentBlock, itemStack)) {
        currentBlock.setType(Material.AIR);
      } else {
        ChallengeHelper.breakBlock(currentBlock, itemStack, player.getInventory());
      }

      // breakBlock leaves the block alone when the drop chance challenge swallows it.
      if (currentBlock.getType() != before) {
        RepeatInChunkChallenge.repeatInEveryChunk(currentBlock);
      }
    }

    private boolean decreaseDurability() {
      if (itemStack.getType() == Material.AIR) return true;
      if (!isTool(itemStack)) return true;


      PlayerInventory inventory = player.getInventory();
      int slot = IntStream.range(0, inventory.getSize()).filter(index -> itemStack.isSimilar(inventory.getItem(index))).findFirst().orElse(-1);
      if (slot == -1) return false;

      itemStack = inventory.getItem(slot);
      ItemUtils.damageTool(player, slot);
      return true;
    }

    private boolean isTool(@NotNull ItemStack itemStack) {
      if (itemStack.getItemMeta() != null) {
        try {
          Attribute attribute = Attribute.MAX_HEALTH;
          Collection<AttributeModifier> attributeModifiers = itemStack.getItemMeta().getAttributeModifiers(attribute);
          return attributeModifiers == null || !attributeModifiers.isEmpty();

        } catch (NullPointerException exception) {
          return false;
        }
      }
      return true;
    }

    private boolean cantBeBroken(@NotNull Block block, @NotNull ItemStack tool) {
      return block.getDrops(tool).isEmpty();
    }

  }

  private static class DropLoop implements Loop {

    private final ItemStack itemStack;
    private final Player player;

    public DropLoop(ItemStack itemStack, Player player) {
      itemStack = itemStack.clone();
      itemStack.setAmount(1);
      this.itemStack = itemStack;
      this.player = player;
    }

    @Override
    public void execute() {
      if (ignorePlayer(player)) return;

      if (!decreaseItem()) {
        cancel();
        return;
      }

      InventoryUtils.dropItemByPlayer(player.getLocation(), itemStack);
    }

    private boolean decreaseItem() {
      ItemStack item = Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(itemStack -> itemStack.isSimilar(this.itemStack)).findFirst().orElse(null);
      if (item == null) return false;
      item.setAmount(item.getAmount() - 1);
      return true;
    }

  }

}
