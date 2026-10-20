package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EntityUtils {

  private EntityUtils() {
  }

  public static Vector getSucceedingVelocity(@NotNull Vector vector) {
    return new Vector(vector.getX(), getSucceedingVelocity(vector.getY()), vector.getX());
  }

  public static double getSucceedingVelocity(double currentYVelocity) {
    return 0.98 * ((currentYVelocity) - 0.08);
  }

  public static boolean isStandingOnBlock(@NotNull Entity entity, Material block) {
    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        for (int y = -1; y <= 1; y++) {
          Material type = entity.getLocation().add(x, y, z).getBlock().getType();
          if (type == block) return true;
        }
      }
    }
    return false;
  }

  @Nullable
  public static Material getSpawnEgg(EntityType type) {
    if (!type.isSpawnable() || !type.isAlive()) return null;
    try {
      return Material.valueOf(type.name() + "_SPAWN_EGG");
    } catch (Exception ex) {
      return null;
    }
  }

}
