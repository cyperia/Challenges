package net.cyperia.challenges.plugin.management.blocks;

import net.cyperia.challenges.plugin.challenges.implementation.setting.CutCleanSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuSetting.SubSetting;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public final class BlockDropManager {

  private final Map<Material, RegisteredDrops> drops = new HashMap<>();
  private final Map<Material, RegisteredChance> chance = new HashMap<>();
  private SubSetting directInventorySetting;

  @NotNull
  public Collection<ItemStack> getDrops(@NotNull Block block, @Nullable ItemStack tool) {
    if (!getDropChance(block.getType()).getAsBoolean()) return new ArrayList<>();
    Collection<ItemStack> customDrops = getCustomDrops(block, tool, null);
    if (customDrops != null) return customDrops;
    return block.getDrops(tool);
  }

  /** Null when no setting replaces this block's drops, empty when one replaces them with nothing. */
  @Nullable
  public Collection<ItemStack> getCustomDrops(@NotNull Block block, @Nullable ItemStack tool, @Nullable Entity breaker) {
    CustomDrops option = getCustomDrops(block.getType());
    return option == null ? null : option.roll(block, tool, breaker);
  }

  @Nullable
  private CustomDrops getCustomDrops(@NotNull Material block) {
    RegisteredDrops option = drops.get(block);
    return option == null ? null : option.getFirst().orElse(null);
  }

  public void setCustomDrops(@NotNull Material block, @NotNull Material item, byte priority) {
    setCustomDrops(block, Collections.singletonList(item), priority);
  }

  public void setCustomDrops(@NotNull Material block, @NotNull List<Material> items, byte priority) {
    Logger.debug("Setting block drop for {} to {} at priority {}", block, items, priority);
    setCustomDrops(block, new ItemDrops(items), priority);
  }

  public void setDropsOf(@NotNull Material block, @NotNull Material source, byte priority) {
    Logger.debug("Setting block {} to drop like {} at priority {}", block, source, priority);
    setCustomDrops(block, new SourceDrops(source), priority);
  }

  private void setCustomDrops(@NotNull Material block, @NotNull CustomDrops customDrops, byte priority) {
    RegisteredDrops option = this.drops.computeIfAbsent(block, key -> new RegisteredDrops());
    option.setOption(priority, customDrops);
  }

  public void resetCustomDrop(@NotNull Material block, byte priority) {
    Logger.debug("Resetting block drop for {} at priority {}", block, priority);

    RegisteredDrops option = drops.get(block);
    if (option == null) return;

    option.resetOption(priority);
    if (option.isEmpty()) drops.remove(block);
  }

  public void resetCustomDrops(byte priority) {
    Logger.debug("Resetting block drops at priority {}", priority);

    List<Material> remove = new ArrayList<>();
    for (Entry<Material, RegisteredDrops> entry : drops.entrySet()) {

      RegisteredDrops option = entry.getValue();
      option.resetOption(priority);
      if (option.isEmpty()) remove.add(entry.getKey());
    }

    remove.forEach(drops::remove);
  }

  @NotNull
  public BooleanSupplier getDropChance(@NotNull Material block) {
    RegisteredChance option = chance.get(block);
    if (option == null) return () -> true;
    return option.getFirst().orElse(() -> true);
  }

  public void setDropChance(@NotNull Material block, byte priority, @NotNull BooleanSupplier chance) {
    Logger.debug("Setting block drop chance for {} at priority {}", block, priority);

    RegisteredChance option = this.chance.computeIfAbsent(block, key -> new RegisteredChance());
    option.setOption(priority, chance);
  }

  public void resetDropChance(byte priority) {
    Logger.debug("Resetting block drop chance at priority " + priority);

    List<Material> remove = new ArrayList<>();
    for (Entry<Material, RegisteredChance> entry : chance.entrySet()) {

      RegisteredChance option = entry.getValue();
      option.resetOption(priority);
      if (option.isEmpty()) remove.add(entry.getKey());
    }

    remove.forEach(chance::remove);
  }

  public boolean isItemsDirectIntoInventory() {
    if (directInventorySetting == null)
      directInventorySetting = AbstractChallenge.getFirstInstance(CutCleanSetting.class).getDirectInventorySetting();
    return directInventorySetting.isEnabled();
  }

  public static final class DropPriority {

    public static final byte
      CUT_CLEAN = 10,
      RANDOMIZER = 5,
      CHANCE = -128;

    private DropPriority() {
    }
  }

  private static abstract class RegisteredOptions<T> {

    private final SortedMap<Byte, T> optionByPriority = new TreeMap<>(Collections.reverseOrder());

    public void setOption(byte priority, @NotNull T option) {
      optionByPriority.put(priority, option);
    }

    public void resetOption(byte priority) {
      optionByPriority.remove(priority);
    }

    @NotNull
    public Optional<T> getFirst() {
      return optionByPriority.values().stream().findFirst();
    }

    public boolean isEmpty() {
      return optionByPriority.isEmpty();
    }

  }

  private sealed interface CustomDrops {

    @NotNull
    Collection<ItemStack> roll(@NotNull Block block, @Nullable ItemStack tool, @Nullable Entity breaker);

  }

  private record ItemDrops(@NotNull List<Material> items) implements CustomDrops {

    @NotNull
    @Override
    public Collection<ItemStack> roll(@NotNull Block block, @Nullable ItemStack tool, @Nullable Entity breaker) {
      return items.stream().map(ItemStack::new).collect(Collectors.toList());
    }

  }

  private record SourceDrops(@NotNull Material source) implements CustomDrops {

    @NotNull
    @Override
    public Collection<ItemStack> roll(@NotNull Block block, @Nullable ItemStack tool, @Nullable Entity breaker) {
      // the broken block decides whether anything drops, as vanilla checks the tool before it rolls the loot table
      if (tool != null && !block.isPreferredTool(tool)) return new ArrayList<>();

      BlockData data = source.createBlockData();
      // crops and beds only drop in one state, a grown crop and a bed's head
      if (data instanceof Ageable ageable) ageable.setAge(ageable.getMaximumAge());
      if (data instanceof Bed bed) bed.setPart(Bed.Part.HEAD);

      ItemStack lootTool = tool;
      if (tool != null && !data.isPreferredTool(tool)) {
        // Paper checks the tool against the source again, so roll with one that passes it and keeps the enchantments
        lootTool = Arrays.stream(STAND_IN_TOOLS).map(ItemStack::new).filter(data::isPreferredTool).findFirst().orElse(null);
        if (lootTool != null) lootTool.addUnsafeEnchantments(tool.getEnchantments());
      }

      BlockState state = block.getState();
      state.setBlockData(data);
      return new ArrayList<>(state.getDrops(lootTool, breaker));
    }

    private static final Material[] STAND_IN_TOOLS = {Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_SWORD};

  }

  private static class RegisteredDrops extends RegisteredOptions<CustomDrops> {
    private RegisteredDrops() {
    }
  }

  public static class RegisteredChance extends RegisteredOptions<BooleanSupplier> {
    private RegisteredChance() {
    }
  }

}
