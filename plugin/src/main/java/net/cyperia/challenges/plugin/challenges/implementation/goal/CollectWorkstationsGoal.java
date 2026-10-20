package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.ItemCollectionGoal;
import org.bukkit.Material;

public class CollectWorkstationsGoal extends ItemCollectionGoal {

  public CollectWorkstationsGoal() {
    super(Material.LECTERN, Material.COMPOSTER, Material.GRINDSTONE, Material.BLAST_FURNACE,
      Material.SMOKER, Material.FLETCHING_TABLE, Material.CARTOGRAPHY_TABLE,
      Material.BREWING_STAND, Material.SMITHING_TABLE, Material.CAULDRON,
      Material.LOOM, Material.STONECUTTER, Material.BARREL
    );
  }

}
