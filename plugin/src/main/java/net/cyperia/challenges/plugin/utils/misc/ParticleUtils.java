package net.cyperia.challenges.plugin.utils.misc;

import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class ParticleUtils {

  private static final IRandom particleRandom = IRandom.create();
  private static final double cylinderParticleVerticalStep = 0.25;

  private ParticleUtils() {
  }

  private static void spawnParticleCircle(@NotNull Location center, int points, double radius, @NotNull BiConsumer<World, Location> playParticle) {
    World world = center.getWorld();
    if (world == null) return;

    for (int i = 0; i < points; i++) {
      double angle = 2 * Math.PI * i / points;
      Location point = center.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));
      playParticle.accept(world, point);
    }
  }

  public static void spawnEffectCircle(@NotNull Location location, @NotNull Effect particle, int points, double radius) {
    spawnParticleCircle(location, points, radius, (world, point) -> world.playEffect(point, particle, 1));
  }

  public static void spawnParticleCircle(@NotNull Location location, @NotNull Particle particle, @Nullable Color overrideColor, int points, double radius) {
    spawnParticleCircle(location, points, radius, (world, loc) -> doSpawnParticleWithData(loc, particle, 1, overrideColor));
  }

  private static void spawnParticleCylinder(@NotNull JavaPlugin plugin, @NotNull Location location,
                                            int points, double radius, double height,
                                            @NotNull BiConsumer<World, Location> playParticle) {
    for (double y = 0, i = 0; y < height; y += cylinderParticleVerticalStep, i++) {
      final double Y = y;
      Regions.later(plugin, location, () -> {
        spawnParticleCircle(location.clone().add(0, Y, 0), points, radius, playParticle);
      }, (long) i);
    }
  }

  public static void spawnEffectCylinder(@NotNull JavaPlugin plugin, @NotNull Location location,
                                         @NotNull Effect effect, int points, double radius, double height) {
    spawnParticleCylinder(plugin, location, points, radius, height, (world, point) -> world.playEffect(point, effect, 1));
  }

  /**
   * The flourish that marks somebody arriving on the server. A vanish plugin can replay it,
   * so a player watching cannot tell a fake arrival from a real one.
   */
  public static void spawnJoinEffect(@NotNull JavaPlugin plugin, @NotNull Player player) {
    spawnParticleCylinder(plugin, player.getLocation(), Particle.ENTITY_EFFECT, 17, 1, 2);
  }

  public static void spawnParticleCylinder(@NotNull JavaPlugin plugin, @NotNull Location location,
                                           @NotNull Particle particle,
                                           int points, double radius, double height) {
    spawnParticleCylinder(plugin, location, particle, null, points, radius, height);
  }

  public static void spawnParticleCylinder(@NotNull JavaPlugin plugin, @NotNull Location location,
                                           @NotNull Particle particle, @Nullable Color overrideColor,
                                           int points, double radius, double height) {
    for (double y = 0, i = 0; y < height; y += .25, i++) {
      final double Y = y;

      Regions.later(plugin, location, () -> spawnParticleCircle(
        location.clone().add(0, Y, 0), points, radius,
        (world, loc) -> doSpawnParticleWithData(loc, particle, 1, overrideColor)
      ), (long) i);
    }
  }

  public static void spawnParticleCircleAroundEntity(@NotNull JavaPlugin plugin, @NotNull Entity entity) {
    spawnParticleCylinderAroundRadius(
      plugin, entity.getLocation(), Particle.INSTANT_EFFECT, Color.WHITE,
      entity.getBoundingBox().getWidthX(), cylinderParticleVerticalStep);
  }

  public static void spawnParticleCylinderAroundRadius(@NotNull JavaPlugin plugin, @NotNull Location location,
                                                       @NotNull Particle particle, @Nullable Color overrideColor,
                                                       double radius, double height) {
    spawnParticleCylinder(plugin, location, particle, overrideColor, (int) (radius * 15), radius, height);
  }

  public static void drawLine(@NotNull Player player, @NotNull Location point1, @NotNull Location point2, @NotNull Particle particle, @Nullable Particle.DustOptions dustOptions, int count, double space, int max) {
    World world = point1.getWorld();
    if (!Objects.equals(world, point2.getWorld())) return;
    double distance = point1.distance(point2);
    Vector p1 = point1.toVector();
    Vector p2 = point2.toVector();
    Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
    double length = 0;
    int current = 0;
    for (; length < distance; p1.add(vector)) {
      player.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), count, dustOptions);
      length += space;

      current++;
      if (current >= max) break;
    }

  }

  /**
   * ONLY SUPPORTS SELECTED PARTICLE TYPES NATIVELY.
   * Adds version specific compatability as api implementation has changed by adding requirements for particle data
   * types like colors etc., by adding them on the fly.
   * As particle type names have also changed, use wrapped types for compatability.
   *
   * @param location      location to spawn particle at (uses referenced world instance)
   * @param particle      particle type to spawn, note changed names and reference via name wrappers for compatability
   * @param count         particle count passed in bukkit call
   * @param overrideColor some particles require a color, fallback to random color if null is passed
   *                      and required by particle data type
   */
  public static void doSpawnParticleWithData(@NotNull Location location, @NotNull Particle particle, int count, @Nullable Color overrideColor) {
    World world = location.getWorld();
    if (world == null) return;

    // Particle#getDataType was added in 1.9 so it should be safe to use
    if (particle.getDataType() == Void.class) { // no data required
      world.spawnParticle(particle, location, count);
      return;
    }
    // ENTITY_EFFECT takes a Color, INSTANT_EFFECT a Particle.Spell

    Object data = null;
    if (particle.getDataType() == Color.class) { // org.bukkit.Color not java.awt
      data = (overrideColor != null) ? overrideColor : newRandomColor();
    }
    // Particle.Spell is experimental API and not handled here

    if (data != null) {
      world.spawnParticle(particle, location, count, data);
      return;
    }

    try {
      // fallback, slow reflection
      Constructor<?> constructor = particle.getDataType().getConstructors()[0];
      Parameter[] parameters = constructor.getParameters();
      Object[] dataParameters = new Object[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        Class<?> type = parameters[i].getType();
        if (type == int.class) {
          dataParameters[i] = 1; // fallback (commonly "size")
        } else if (type == float.class) {
          dataParameters[i] = 1.0f; // fallback (commonly "power")
        } else if (type == double.class) {
          dataParameters[i] = 1.0d;
        } else if (type == Color.class) {
          dataParameters[i] = (overrideColor != null) ? overrideColor : newRandomColor();
        } else {
          // special parameter type not implemented for fallback here, can't provide data
          return;
        }
      }

      data = constructor.newInstance(dataParameters);
      world.spawnParticle(particle, location, count, data);
    } catch (Throwable ex) { // the data type may have no constructor the fallback can fill
      Logger.warn("Unable to create particle {} with data type {}", particle, particle.getDataType(), ex);
    }
  }

  public static Color newRandomColor() {
    return Color.fromRGB(
      particleRandom.range(0, 255),
      particleRandom.range(0, 255),
      particleRandom.range(0, 255)
    );
  }

}
