package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.ItemCollectionGoal;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CollectSwordsGoal extends ItemCollectionGoal {

  public CollectSwordsGoal() {
    List<Material> targets = new ArrayList<>(Arrays.asList(
      Material.WOODEN_SWORD, Material.STONE_SWORD,
      Material.IRON_SWORD, Material.GOLDEN_SWORD,
      Material.DIAMOND_SWORD,
      Material.NETHERITE_SWORD
    ));

    setTarget(targets.toArray(new Object[0]));
  }

}
