package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.management.blocks.BlockDropManager;
import net.cyperia.challenges.plugin.management.blocks.BlockDropManager.DropPriority;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockItemRandomizerChallenge extends RandomizerSetting {

  private final Map<Material, List<Material>> randomization = new LinkedHashMap<>();

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled) getFirstInstance(BlockRandomizerChallenge.class).setEnabled(false);
    super.setEnabled(enabled);
  }

  @Override
  protected void reloadRandomization() {
    randomization.clear();
    BlockDropManager manager = Challenges.getInstance().getBlockDropManager();

    List<Material> blocks = new ArrayList<>(Arrays.asList(ExperimentalUtils.getMaterials()));
    blocks.removeIf(material -> !ItemUtils.isObtainableInSurvival(material) || !material.isBlock() || material.isAir());
    random.shuffle(blocks);

    List<Material> drops = new ArrayList<>(Arrays.asList(ExperimentalUtils.getMaterials()));
    drops.removeIf(material -> !material.isItem() || !ItemUtils.isObtainableInSurvival(material));
    random.shuffle(drops);

    while (!blocks.isEmpty()) {
      Material block = blocks.remove(0);
      List<Material> items = new ArrayList<>();

      int addDrops = getMatches(blocks.size(), drops.size());
      for (int i = 0; i < addDrops && !drops.isEmpty(); i++) {
        items.add(drops.remove(0));
      }
      if (items.isEmpty()) continue;
      manager.setCustomDrops(block, items, DropPriority.RANDOMIZER);
      randomization.put(block, items);
    }

  }

  @NotNull
  @Override
  public List<Pairing> getPairings() {
    return randomization.entrySet().stream().map(entry -> new Pairing(entry.getKey(), entry.getValue())).toList();
  }

  @Override
  protected void onDisable() {
    randomization.clear();
    BlockDropManager manager = Challenges.getInstance().getBlockDropManager();
    manager.resetCustomDrops(DropPriority.RANDOMIZER);
  }

}
