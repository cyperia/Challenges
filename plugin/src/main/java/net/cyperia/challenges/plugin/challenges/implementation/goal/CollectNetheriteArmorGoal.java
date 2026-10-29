package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.ItemCollectionGoal;
import org.bukkit.Material;

public class CollectNetheriteArmorGoal extends ItemCollectionGoal {

  public CollectNetheriteArmorGoal() {
    super(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);
  }

}
