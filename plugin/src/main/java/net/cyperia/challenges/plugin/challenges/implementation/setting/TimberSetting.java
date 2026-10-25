package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.utils.misc.WoodSpecies;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TimberSetting extends SettingModifier {

  public static final int VALUE_STEM = 1;
  public static final int VALUE_WHOLE_TREE = 2;

  /**
   * Trees grow diagonally (branches of big oaks, jungle and dark oak trees), so the flood fill has to
   * look at all 26 surrounding blocks instead of only at the six touching faces.
   */
  private static final int[][] NEIGHBOUR_OFFSETS = createNeighbourOffsets();

  /**
   * Vanilla leaves survive up to six blocks away from a log, everything further away belongs to another
   * tree. Bounding the canopy search this way also keeps a single break from sweeping a whole forest.
   */
  private static final int MAX_CANOPY_DISTANCE = 6;

  /**
   * Upper bound of blocks a single tree may consist of. Every vanilla tree fits into this by a wide
   * margin (the largest jungle tree stays below 700 blocks); the limit only stops player built log
   * structures from being torn down entirely and caps the work a single break can cause.
   */
  private static final int MAX_TREE_SIZE = 1024;

  /**
   * How many blocks are removed per tick. High enough for even the largest tree to be gone within a few
   * seconds, low enough for the tree to visibly fall instead of vanishing at once.
   */
  private static final int BLOCKS_PER_TICK = 12;

  public TimberSetting() {
    super(VALUE_WHOLE_TREE);
    setValueUnit(namedValues("settings.logs", "settings.logs-leaves"));
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return new ItemStack(getValue() == VALUE_WHOLE_TREE ? Material.OAK_LEAVES : Material.OAK_LOG);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(@NotNull BlockBreakEvent event) {
    if (!shouldExecuteEffect()) return;

    Block origin = event.getBlock();
    WoodSpecies species = WoodSpecies.ofLog(origin.getType());
    if (species == null) return;

    Player player = event.getPlayer();
    if (ignorePlayer(player)) return;

    // the origin is still intact at this point, it is only turned into air once the event has been handled
    List<Block> tree = searchTree(origin, species, getValue() == VALUE_WHOLE_TREE);
    if (tree.size() <= 1) return;

    fellTree(player, species, tree.subList(1, tree.size()));
  }

  /**
   * Collects every block of the tree the given log belongs to, breadth first, so that the tree falls
   * from the break outwards.
   *
   * <p>The stem is searched first and without a depth limit, afterwards the canopy is searched in layers
   * seeded by all found logs, which gives every canopy block its distance to the closest log for free.
   *
   * @param origin        the log that was broken, always the first element of the result
   * @param species       the species every collected block has to belong to
   * @param includeCanopy whether the canopy should be collected in addition to the stem
   * @return the blocks of the tree, starting with the given origin
   */
  @NotNull
  private static List<Block> searchTree(@NotNull Block origin, @NotNull WoodSpecies species, boolean includeCanopy) {
    List<Block> tree = new ArrayList<>();
    Set<Block> visited = new HashSet<>();
    tree.add(origin);
    visited.add(origin);

    // stem: only logs are marked as visited, so the canopy search below can still look at everything else
    for (int index = 0; index < tree.size(); index++) {
      Block current = tree.get(index);
      for (int[] offset : NEIGHBOUR_OFFSETS) {
        Block neighbour = current.getRelative(offset[0], offset[1], offset[2]);
        if (!species.isLog(neighbour.getType())) continue;
        if (!visited.add(neighbour)) continue;
        tree.add(neighbour);
        if (tree.size() >= MAX_TREE_SIZE) return tree;
      }
    }

    if (!includeCanopy) return tree;

    // canopy: one layer per iteration, every block of a layer is one step further away from the stem
    int layerStart = 0;
    int layerEnd = tree.size();
    for (int distance = 1; distance <= MAX_CANOPY_DISTANCE && layerStart < layerEnd; distance++) {
      for (int index = layerStart; index < layerEnd; index++) {
        Block current = tree.get(index);
        for (int[] offset : NEIGHBOUR_OFFSETS) {
          Block neighbour = current.getRelative(offset[0], offset[1], offset[2]);
          if (!visited.add(neighbour)) continue;
          if (!isNaturalCanopy(neighbour, species)) continue;
          tree.add(neighbour);
          if (tree.size() >= MAX_TREE_SIZE) return tree;
        }
      }
      layerStart = layerEnd;
      layerEnd = tree.size();
    }

    return tree;
  }

  /**
   * Removes the given blocks over the next ticks, {@value #BLOCKS_PER_TICK} of them at a time.
   */
  private void fellTree(@NotNull Player player, @NotNull WoodSpecies species, @NotNull List<Block> tree) {
    Iterator<Block> blocks = tree.iterator();
    Regions.timer(plugin, tree.getFirst().getLocation(), task -> {
      if (!player.isOnline()) {
        task.cancel();
        return;
      }

      for (int i = 0; i < BLOCKS_PER_TICK && blocks.hasNext(); i++) {
        if (!breakBlock(player, species, blocks.next())) {
          task.cancel();
          return;
        }
      }

      if (!blocks.hasNext()) task.cancel();
    }, 1, 1);
  }

  /**
   * @return {@code false} if the tool broke and the tree should stop falling
   */
  private static boolean breakBlock(@NotNull Player player, @NotNull WoodSpecies species, @NotNull Block block) {
    Material type = block.getType();
    // the world may have changed between the search and this tick
    if (!species.isCanopy(type) && !species.isLog(type)) return true;

    // read the tool freshly every time, the player may swap or drop it while the tree is falling
    PlayerInventory inventory = player.getInventory();
    ItemStack tool = inventory.getItemInMainHand();
    ChallengeHelper.breakBlock(block, tool, inventory);

    // as in vanilla, breaking leaves is free, only the logs wear the tool down
    if (!species.isLog(type) || !isDamageable(tool)) return true;
    // these blocks are not broken by the player, so vanilla never wears the tool down
    return ItemUtils.damageTool(player, inventory.getHeldItemSlot());
  }

  private static boolean isDamageable(@NotNull ItemStack tool) {
    Damageable meta = ItemUtils.getDamageableMeta(tool);
    if (meta == null || meta.isUnbreakable()) return false;
    return !getFirstInstance(NoItemDamageSetting.class).isEnabled();
  }

  /**
   * Leaves placed by a player are marked as persistent and never decay, which makes them part of a build
   * and not part of a naturally grown tree.
   */
  private static boolean isNaturalCanopy(@NotNull Block block, @NotNull WoodSpecies species) {
    if (!species.isCanopy(block.getType())) return false;
    return !(block.getBlockData() instanceof Leaves leaves) || !leaves.isPersistent();
  }

  @NotNull
  private static int[][] createNeighbourOffsets() {
    int[][] offsets = new int[26][];
    int index = 0;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          if (x == 0 && y == 0 && z == 0) continue;
          offsets[index++] = new int[]{x, y, z};
        }
      }
    }
    return offsets;
  }

}
