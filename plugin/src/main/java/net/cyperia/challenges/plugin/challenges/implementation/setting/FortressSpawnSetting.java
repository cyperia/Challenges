package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.NetherPortalSpawnSetting;
import org.bukkit.Material;
import org.bukkit.StructureType;

public class FortressSpawnSetting extends NetherPortalSpawnSetting {

  public FortressSpawnSetting() {
    super(StructureType.NETHER_FORTRESS, "unable-to-find-fortress", Material.NETHER_BRICKS);
  }

}
