package net.cyperia.challenges.plugin.utils.misc;

import io.papermc.paper.world.flag.FeatureDependant;
import net.cyperia.challenges.plugin.Challenges;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ExperimentalUtils {

  private static Material[] materials;
  private static EntityType[] entityTypes;

  public static Material[] getMaterials() {
    if (materials == null) {
      loadMaterials();
    }
    return materials;
  }

  private static void loadMaterials() {
    List<Material> materials = new LinkedList<>();

    World world = Challenges.getInstance().getGameWorldStorage().getWorld(World.Environment.NORMAL);
    for (Material material : Material.values()) {
      if (!isMaterialFeatureEnabled(material, world)) continue;
      materials.add(material);
    }
    ExperimentalUtils.materials = materials.toArray(new Material[0]);
  }

  private static boolean isMaterialFeatureEnabled(@NotNull Material material, @NotNull World world) {
    FeatureDependant feature = material.isItem() ? material.asItemType() : material.asBlockType();
    return feature == null || world.isEnabled(feature);
  }

  public static EntityType[] getEntityTypes() {
    if (entityTypes == null) {
      loadEntityTypes();
    }
    return entityTypes;
  }

  private static void loadEntityTypes() {
    World world = Challenges.getInstance().getGameWorldStorage().getWorld(World.Environment.NORMAL);
    ExperimentalUtils.entityTypes = enabledEntityTypes(world);
  }

  static EntityType[] enabledEntityTypes(@NotNull World world) {
    List<EntityType> entityTypes = new LinkedList<>();
    for (EntityType type : EntityType.values()) {
      if (!isEntityTypeFeatureEnabled(type, world)) continue;

      entityTypes.add(type);
    }
    return entityTypes.toArray(new EntityType[0]);
  }

  private static boolean isEntityTypeFeatureEnabled(@NotNull EntityType entityType, @NotNull World world) {
    return entityType != EntityType.UNKNOWN && world.isEnabled(entityType);
  }

}
