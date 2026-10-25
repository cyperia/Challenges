package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.*;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceStructureAction extends EntityTargetAction {

  private volatile List<NamespacedKey> structureKeys;
  private volatile List<NamespacedKey> villageKeys;

  public PlaceStructureAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(false, true).addChild(SubSettingsHelper.createStructureSettingsBuilder()));
  }

  @Override
  public void executeFor(Entity entity, Map<String, String[]> subActions) {
    String structureString = subActions.get(SubSettingsHelper.STRUCTURE)[0];

    NamespacedKey structureKey;

    if (structureString.equals("random_structure")) {
      if (structureKeys == null) {
        reloadStructureKeys();
      }

      structureKey = structureKeys.get(IRandom.singleton().nextInt(structureKeys.size()));
      if (structureKey == StructureType.VILLAGE.getKey()) {
        structureKey = villageKeys.get(IRandom.singleton().nextInt(villageKeys.size()));
      }
    } else {
      StructureType structureType = StructureType.getStructureTypes().get(structureString);
      structureKey = getStructureKey(structureType);
    }


    Location location = entity.getLocation();
    String locationString = (int) location.getX() + " " + (int) location.getY() + " " + (int) location.getZ();
    String command = String.format("minecraft:place structure %s %s", structureKey, locationString);
    Regions.global(Challenges.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
  }

  private void reloadStructureKeys() {
    List<NamespacedKey> structures = StructureType.getStructureTypes().values().stream()
      .map(StructureType::getKey)
      .collect(Collectors.toList());

    structures.remove(StructureType.OCEAN_RUIN.getKey());
    structures.add(NamespacedKey.minecraft("ocean_ruin_cold"));
    structures.add(NamespacedKey.minecraft("ocean_ruin_warm"));

    List<NamespacedKey> villages = new ArrayList<>();
    villages.add(NamespacedKey.minecraft("village_desert"));
    villages.add(NamespacedKey.minecraft("village_plains"));
    villages.add(NamespacedKey.minecraft("village_savanna"));
    villages.add(NamespacedKey.minecraft("village_snowy"));
    villages.add(NamespacedKey.minecraft("village_taiga"));

    // Entity threads can enter concurrently; villageKeys must be set before structureKeys is visible.
    villageKeys = villages;
    structureKeys = structures;
  }

  private NamespacedKey getStructureKey(StructureType structureType) {
    if (structureType == StructureType.OCEAN_RUIN) {
      List<NamespacedKey> oceanRuins = Arrays.asList(NamespacedKey.minecraft("ocean_ruin_cold"), NamespacedKey.minecraft("ocean_ruin_warm"));
      return oceanRuins.get(IRandom.singleton().nextInt(oceanRuins.size()));
    }
    if (structureType == StructureType.VILLAGE) {
      return villageKeys.get(IRandom.singleton().nextInt(villageKeys.size()));
    }
    return structureType.getKey();
  }

}
