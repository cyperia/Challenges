package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.management.blocks.BlockDropManager;
import net.cyperia.challenges.plugin.management.blocks.BlockDropManager.DropPriority;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockRandomizerChallenge extends RandomizerSetting {

  private final Map<Material, Material> randomization = new LinkedHashMap<>();

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled) getFirstInstance(BlockItemRandomizerChallenge.class).setEnabled(false);
    super.setEnabled(enabled);
  }

  @Override
  protected void reloadRandomization() {
    randomization.clear();
    List<Material> blocks = new ArrayList<>(Arrays.asList(ExperimentalUtils.getMaterials()));
    blocks.removeIf(material -> !material.isBlock() || material.isLegacy() || material.isAir());

    // a one to one swap among the placeable blocks keeps every one of their drops reachable through exactly one block
    List<Material> placeable = new ArrayList<>(blocks);
    placeable.removeIf(material -> !material.isItem() || !ItemUtils.isObtainableInSurvival(material));
    List<Material> sources = new ArrayList<>(placeable);
    random.shuffle(sources);

    Map<Material, Material> sourceOf = new HashMap<>();
    for (int i = 0; i < placeable.size(); i++) {
      sourceOf.put(placeable.get(i), sources.get(i));
    }

    BlockDropManager manager = Challenges.getInstance().getBlockDropManager();
    for (Material block : blocks) {
      Material source = resolveSource(block, sourceOf, sources);
      if (source == null) continue;
      manager.setDropsOf(block, source, DropPriority.RANDOMIZER);
      randomization.put(block, source);
    }
  }

  @NotNull
  @Override
  public List<Pairing> getPairings() {
    return pairingsOf(randomization);
  }

  @Nullable
  private Material resolveSource(@NotNull Material block, @NotNull Map<Material, Material> sourceOf, @NotNull List<Material> sources) {
    if (sourceOf.containsKey(block)) return sourceOf.get(block);

    Material source;
    String name = block.name();
    Material tip = name.endsWith("_PLANT") ? Material.getMaterial(name.substring(0, name.length() - "_PLANT".length())) : null;
    Material placement = block.createBlockData().getPlacementMaterial();
    if (tip != null) {
      // the body of a growing plant, like a kelp plant, drops like its tip
      source = resolveSource(tip, sourceOf, sources);
    } else if (sourceOf.containsKey(placement)) {
      source = sourceOf.get(placement);
    } else if (placement.isAir()) {
      // blocks without any item, like portals or fire, drop nothing in vanilla and must keep it that way
      source = null;
    } else {
      source = random.choose(sources);
    }

    sourceOf.put(block, source);
    return source;
  }

  @Override
  protected void onDisable() {
    randomization.clear();
    Challenges.getInstance().getBlockDropManager().resetCustomDrops(DropPriority.RANDOMIZER);
  }

}
