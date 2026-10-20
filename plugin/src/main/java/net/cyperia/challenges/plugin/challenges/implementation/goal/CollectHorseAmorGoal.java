package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.ItemCollectionGoal;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CollectHorseAmorGoal extends ItemCollectionGoal {

  public CollectHorseAmorGoal() {
    List<Material> targets = new ArrayList<>(Arrays.asList(
      Material.DIAMOND_HORSE_ARMOR,
      Material.GOLDEN_HORSE_ARMOR,
      Material.IRON_HORSE_ARMOR,
      Material.LEATHER_HORSE_ARMOR
    ));

    setTarget(targets.toArray(new Object[0]));
  }

}
