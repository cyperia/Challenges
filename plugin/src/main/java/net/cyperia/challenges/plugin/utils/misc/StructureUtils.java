package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.Material;
import org.bukkit.StructureType;

public class StructureUtils {
  public static Material getStructureIcon(StructureType structureType) {

    switch (structureType.getName()) {
      case "nether_fossil":
        return Material.BONE_BLOCK;
      case "jungle_pyramid":
        return Material.LEVER;
      case "monument":
        return Material.SEA_LANTERN;
      case "desert_pyramid":
        return Material.SANDSTONE_STAIRS;
      case "mineshaft":
        return Material.RAIL;
      case "ocean_ruin":
        return Material.DROWNED_SPAWN_EGG;
      case "bastion_remnant":
        return Material.BLACKSTONE;
      case "shipwreck":
        return Material.OAK_BOAT;
      case "fortress":
        return Material.NETHER_BRICK_FENCE;
      case "buried_treasure":
        return Material.CHEST;
      case "pillager_outpost":
        return Material.CROSSBOW;
      case "swamp_hut":
        return Material.CAULDRON;
      case "igloo":
        return Material.SNOW_BLOCK;
      case "village":
        return Material.VILLAGER_SPAWN_EGG;
      case "stronghold":
        return Material.END_PORTAL_FRAME;
      case "end_city":
        return Material.PURPUR_PILLAR;
      case "mansion":
        return Material.TOTEM_OF_UNDYING;
      case "ruined_portal":
        return Material.CRYING_OBSIDIAN;
      default:
        return Material.STRUCTURE_VOID;
    }
  }

}
