package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuSetting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.utils.misc.ListBuilder;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AllBlocksDisappearChallenge extends MenuSetting {

  private final int stackDropLimit;
  private final BooleanSubSetting breakSetting, placeSetting;

  public AllBlocksDisappearChallenge() {
    breakSetting = registerSetting("break", new BooleanSubSetting(
      new ItemStack(Material.DIAMOND_PICKAXE), getSubSettingSpace("break"),
      true
    ));
    placeSetting = registerSetting("place", new BooleanSubSetting(
      new ItemStack(Material.DIAMOND_BLOCK), getSubSettingSpace("place")
    ));

    stackDropLimit = Challenges.getInstance().getConfigDocument().getInt(ConfigManager.Keys.CHALLENGE_SETTINGS_ALL_BLOCKS_DISAPPEAR_STACK_DROP_LIMIT, 50);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(@NotNull BlockBreakEvent event) {
    if (!breakSetting.getAsBoolean()) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    PlayerInventory inventory = event.getPlayer().getInventory();
    event.setDropItems(false);
    breakBlocks(event.getBlock(), inventory.getItemInMainHand(), inventory);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(@NotNull BlockPlaceEvent event) {
    if (!placeSetting.getAsBoolean()) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getBlockAgainst().getType() == Material.BEDROCK) return;
    if (event.getBlockAgainst().getType() == Material.END_PORTAL) return;
    breakBlocks(event.getBlockAgainst(), null, event.getPlayer().getInventory());
  }

  private void breakBlocks(@NotNull Block block, @Nullable ItemStack tool, @NotNull Inventory inventory) {
    Chunk chunk = block.getChunk();
    List<Block> blocks = getAllBlocksToBreak(chunk, block.getType());

    List<ItemStack> allDrops = new ArrayList<>();

    for (Block current : blocks) {
      Collection<ItemStack> drops = Challenges.getInstance().getBlockDropManager().getDrops(current, tool);
      current.setType(Material.AIR);

      for (ItemStack currentBlockDrop : drops) {
        boolean containsType = false;

        for (ItemStack currentAllDrop : allDrops) {
          if (currentAllDrop.getType() == currentBlockDrop.getType() && (currentAllDrop.getAmount() + currentBlockDrop.getAmount()) <= currentAllDrop.getMaxStackSize()) {
            containsType = true;
            currentAllDrop.setAmount(currentAllDrop.getAmount() + currentBlockDrop.getAmount());
          }
        }

        if (!containsType) {
          allDrops.add(currentBlockDrop);
        }

      }

    }

    dropList(allDrops, block.getLocation(), inventory);
  }

  private void dropList(@NotNull Collection<ItemStack> itemStacks, @NotNull Location location, @NotNull Inventory inventory) {
    if (location.getWorld() == null) return;
    Map<Material, Integer> stackCount = new HashMap<>();

    for (ItemStack itemStack : itemStacks) {
      if (increaseStackCount(stackCount, itemStack.getType())) {
        ChallengeHelper.dropItem(itemStack, location, inventory);
        location.getWorld().dropItemNaturally(location, itemStack);
      }
    }
  }

  private boolean increaseStackCount(Map<Material, Integer> map, Material material) {
    int droppedStacks = map.getOrDefault(material, 0);
    if (droppedStacks >= stackDropLimit) return false;
    droppedStacks++;
    map.put(material, droppedStacks);
    return true;
  }

  protected List<Block> getAllBlocksToBreak(@NotNull Chunk chunk, @NotNull Material material) {
    return new ListBuilder<Block>()
      .fill(builder -> {
        for (int x = 0; x < 16; x++) {
          for (int z = 0; z < 16; z++) {
            for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
              Block block = chunk.getBlock(x, y, z);
              if (block.getType() == material) {
                builder.add(block);
              }
            }
          }
        }
      })
      .build();
  }

}
