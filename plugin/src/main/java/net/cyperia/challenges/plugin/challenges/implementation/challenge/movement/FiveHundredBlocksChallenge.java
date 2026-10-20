package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class FiveHundredBlocksChallenge extends SettingModifier {

  private final Map<UUID, Integer> blocksWalked = new HashMap<>();

  public FiveHundredBlocksChallenge() {
    super(1, 5, 5);
    setValueUnit(ValueUnit.BLOCKS.scaled(100).describedBy(getChallengeMessageKey("settings-desc")));

    Challenges.getInstance().registerListener(new Listener() {

      @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
      public void onLootGenerate(LootGenerateEvent event) {
        if (!shouldExecuteEffect())
          return;
        event.setLoot(new LinkedList<>());
      }
    });
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bar, player) -> {
      int walked = blocksWalked.getOrDefault(player.getUniqueId(), 0);
      bar.setTitle(getChallengeMessageKey("bossbar"), walked, getBlocksToWalk());
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  private int getBlocksToWalk() {
    return getValue() * 100;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;
    if (BlockUtils.isSameBlockLocationIgnoreHeight(event.getFrom(), event.getTo())) return;

    if (updateOrReset(player)) {
      InventoryUtils.giveItem(player.getInventory(), player.getLocation(), InventoryUtils.getRandomItem(false, false));
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    event.setDropItems(false);
    event.setExpToDrop(0);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityDeath(EntityDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    event.getDrops().clear();
    event.setDroppedExp(0);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityExplosion(EntityExplodeEvent event) {
    if (!shouldExecuteEffect()) return;
    for (Block block : event.blockList()) {
      block.setType(Material.AIR);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockExplosion(BlockExplodeEvent event) {
    if (!shouldExecuteEffect()) return;
    for (Block block : event.blockList()) {
      block.setType(Material.AIR);
    }
  }

  /**
   * @return if 500 blocks were reached
   */
  private boolean updateOrReset(@NotNull Player player) {
    UUID uuid = player.getUniqueId();

    int blocksWalked = this.blocksWalked.getOrDefault(uuid, 0);

    blocksWalked++;

    boolean reached = false;
    if (blocksWalked >= getBlocksToWalk()) {
      blocksWalked = 0;
      reached = true;
    }

    this.blocksWalked.put(uuid, blocksWalked);

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      bossbar.update(player);
    });
    return reached;
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    blocksWalked.clear();
    for (String key : document.keys()) {
      try {
        UUID uuid = UUID.fromString(key);
        int blocks = document.getInt(key);
        blocksWalked.put(uuid, blocks);
      } catch (IllegalArgumentException exception) {
        plugin.getLogger().severe("Error while loading 500 Blocks Challenge, "
          + "key '" + key + "' is not a valid uuid");
        Challenges.getInstance().getILogger().error("", exception);
      }
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    blocksWalked.forEach((uuid, integer) -> {
      document.set(uuid.toString(), integer);
    });
  }

}
