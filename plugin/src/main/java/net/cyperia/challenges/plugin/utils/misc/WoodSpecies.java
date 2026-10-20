package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The kind of wood a tree is made of, together with every block such a tree consists of:
 * its {@link #isLog(Material) logs}, which form the stem and the branches, and its
 * {@link #isCanopy(Material) canopy}, which are the leaves and their nether fungus equivalents.
 *
 * <p>Species are derived once on class initialization from the vanilla {@code minecraft:logs} and
 * {@code minecraft:leaves} block tags. Every log is grouped by its name without the {@code STRIPPED_}
 * prefix and without the {@code _LOG}/{@code _WOOD}/{@code _STEM}/{@code _HYPHAE} suffix, so
 * {@code OAK_LOG}, {@code OAK_WOOD} and {@code STRIPPED_OAK_LOG} all belong to {@code OAK}, which is
 * then matched against {@code OAK_LEAVES}. Wood types added by a later minecraft version or by a
 * datapack are picked up automatically as long as they follow that naming scheme; anything that cannot
 * be mapped forms a species of its own instead of being grouped with an unrelated tree.
 *
 * <p>Every lookup afterwards is a constant time, allocation free {@link EnumMap}/{@link EnumSet} access,
 * which matters because {@link #ofLog(Material)} runs for every single block broken on the server.
 *
 * <p>This is unrelated to Bukkit's legacy {@code TreeSpecies} enum, which only knows the six wood types
 * that existed in minecraft 1.12.
 */
public final class WoodSpecies {

  private static final String STRIPPED_PREFIX = "STRIPPED_";
  private static final String LEAVES_SUFFIX = "_LEAVES";
  private static final String[] LOG_SUFFIXES = {"_LOG", "_WOOD", "_STEM", "_HYPHAE"};

  /**
   * Canopy blocks which are not named after their species and can therefore not be derived from the
   * {@code minecraft:leaves} tag. Referenced by name because not all of them exist on every supported
   * server version.
   */
  private static final Map<String, String[]> ADDITIONAL_CANOPY = Map.of(
    "OAK", new String[]{"AZALEA_LEAVES", "FLOWERING_AZALEA_LEAVES"}, // azalea trees grow an oak stem
    "CRIMSON", new String[]{"NETHER_WART_BLOCK", "SHROOMLIGHT"},
    "WARPED", new String[]{"WARPED_WART_BLOCK", "SHROOMLIGHT"},
    "MANGROVE", new String[]{"MANGROVE_PROPAGULE"},
    "PALE_OAK", new String[]{"PALE_HANGING_MOSS"}
  );

  private static final Map<Material, WoodSpecies> BY_LOG = createSpecies();

  private final String name;
  private final Set<Material> logs;
  private final Set<Material> canopy;

  private WoodSpecies(@NotNull String name, @NotNull Set<Material> logs, @NotNull Set<Material> canopy) {
    this.name = name;
    this.logs = logs;
    this.canopy = canopy;
  }

  /**
   * @param material the material of the block to resolve
   * @return the species the given log belongs to, or {@code null} if the material is not a log at all
   */
  @Nullable
  @Contract(pure = true)
  public static WoodSpecies ofLog(@NotNull Material material) {
    return BY_LOG.get(material);
  }

  /**
   * @return the name of this species, e.g. {@code OAK} or {@code DARK_OAK}
   */
  @NotNull
  @Contract(pure = true)
  public String getName() {
    return name;
  }

  /**
   * @return whether the given material is one of the logs this species' stem and branches are built of,
   * including its bark and stripped variants
   */
  @Contract(pure = true)
  public boolean isLog(@NotNull Material material) {
    return logs.contains(material);
  }

  /**
   * @return whether the given material is one of the blocks this species' canopy is built of
   */
  @Contract(pure = true)
  public boolean isCanopy(@NotNull Material material) {
    return canopy.contains(material);
  }

  @Override
  public String toString() {
    return "WoodSpecies[" + name + "]";
  }

  @NotNull
  private static Map<Material, WoodSpecies> createSpecies() {
    Map<String, EnumSet<Material>> logsBySpecies = new LinkedHashMap<>();
    for (Material log : Tag.LOGS.getValues()) {
      logsBySpecies.computeIfAbsent(speciesNameOf(log), name -> EnumSet.noneOf(Material.class)).add(log);
    }

    Map<String, EnumSet<Material>> canopyBySpecies = new LinkedHashMap<>();
    for (Material leaves : Tag.LEAVES.getValues()) {
      String name = withoutSuffix(leaves.name(), LEAVES_SUFFIX);
      if (name == null || !logsBySpecies.containsKey(name)) continue;
      canopyBySpecies.computeIfAbsent(name, key -> EnumSet.noneOf(Material.class)).add(leaves);
    }
    ADDITIONAL_CANOPY.forEach((name, materials) -> {
      if (!logsBySpecies.containsKey(name)) return;
      EnumSet<Material> canopy = canopyBySpecies.computeIfAbsent(name, key -> EnumSet.noneOf(Material.class));
      for (String material : materials) {
        Material resolved = Utils.getMaterial(material);
        if (resolved != null) canopy.add(resolved);
      }
    });

    Map<Material, WoodSpecies> byLog = new EnumMap<>(Material.class);
    logsBySpecies.forEach((name, logs) -> {
      Set<Material> canopy = canopyBySpecies.getOrDefault(name, EnumSet.noneOf(Material.class));
      WoodSpecies species = new WoodSpecies(name, logs, canopy);
      for (Material log : logs) {
        byLog.put(log, species);
      }
    });
    return byLog;
  }

  @NotNull
  @Contract(pure = true)
  private static String speciesNameOf(@NotNull Material log) {
    String name = log.name();
    if (name.startsWith(STRIPPED_PREFIX)) {
      name = name.substring(STRIPPED_PREFIX.length());
    }
    for (String suffix : LOG_SUFFIXES) {
      String species = withoutSuffix(name, suffix);
      if (species != null) return species;
    }
    return name;
  }

  @Nullable
  @Contract(pure = true)
  private static String withoutSuffix(@NotNull String name, @NotNull String suffix) {
    return name.endsWith(suffix) ? name.substring(0, name.length() - suffix.length()) : null;
  }

}
