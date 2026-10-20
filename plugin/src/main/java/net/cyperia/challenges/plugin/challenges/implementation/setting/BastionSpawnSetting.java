package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.NetherPortalSpawnSetting;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import org.bukkit.StructureType;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BastionSpawnSetting extends NetherPortalSpawnSetting {

  public BastionSpawnSetting() {
    super(StructureType.BASTION_REMNANT, "unable-to-find-bastion",
      Arrays.stream(ExperimentalUtils.getMaterials()).filter(material -> material.name().contains("BASALT")).collect(Collectors.toList()));
  }

}
