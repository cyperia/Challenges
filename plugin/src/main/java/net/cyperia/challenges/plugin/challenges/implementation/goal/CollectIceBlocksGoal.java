package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.ItemCollectionGoal;
import org.bukkit.Material;

public class CollectIceBlocksGoal extends ItemCollectionGoal {

  public CollectIceBlocksGoal() {
    super(Material.ICE, Material.BLUE_ICE, Material.PACKED_ICE, Material.SNOW_BLOCK);
  }

}
