package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.EntityTargetAction;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.*;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceStructureAction extends EntityTargetAction {

  private List<NamespacedKey> structureKeys;
  private List<NamespacedKey> villageKeys;

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
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
  }

  private void reloadStructureKeys() {
    structureKeys = StructureType.getStructureTypes().values().stream()
      .map(StructureType::getKey)
      .collect(Collectors.toList());

    structureKeys.remove(StructureType.OCEAN_RUIN.getKey());
    structureKeys.add(NamespacedKey.minecraft("ocean_ruin_cold"));
    structureKeys.add(NamespacedKey.minecraft("ocean_ruin_warm"));

    villageKeys = new ArrayList<>();
    villageKeys.add(NamespacedKey.minecraft("village_desert"));
    villageKeys.add(NamespacedKey.minecraft("village_plains"));
    villageKeys.add(NamespacedKey.minecraft("village_savanna"));
    villageKeys.add(NamespacedKey.minecraft("village_snowy"));
    villageKeys.add(NamespacedKey.minecraft("village_taiga"));
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
