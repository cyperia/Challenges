package net.cyperia.challenges.plugin.management.server;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GameWorldStorage {

  private static final Environment[] DIMENSIONS = {Environment.NORMAL, Environment.NETHER, Environment.THE_END};

  private final Map<Environment, World> generatedWorlds = new EnumMap<>(Environment.class);
  private List<World> gameWorlds = Collections.emptyList();
  private World overworld;
  private World nether;
  private World end;

  public void enable() {

    for (int i = 0; i < 3 && i < Bukkit.getWorlds().size(); i++) {
      World world = Bukkit.getWorlds().get(i);
      switch (i) {
        case 0: {
          overworld = world;
        }
        break;
        case 1: {
          if (world.getEnvironment() == Environment.NETHER) {
            nether = world;
          } else if (world.getEnvironment() == Environment.THE_END) {
            end = world;
          }
        }
        break;
        case 2: {
          if (world.getEnvironment() == Environment.THE_END) {
            end = world;
          }
        }
        break;
      }
    }
    updateGameWorlds();
  }

  /**
   * Replaces the server's world of every environment {@code generators} returns a generator for.
   */
  public void useGeneratedWorlds(@NotNull String namePrefix, @NotNull Function<Environment, @Nullable ChunkGenerator> generators) {
    List<World> previousGameWorlds = getGameWorlds();
    generatedWorlds.clear();
    for (Environment environment : DIMENSIONS) {
      ChunkGenerator generator = generators.apply(environment);
      if (generator == null) continue;
      generatedWorlds.put(environment, createGeneratedWorld(getGeneratedWorldName(namePrefix, environment), environment, generator));
    }
    updateGameWorlds();
    moveOutOfFormerGameWorlds(previousGameWorlds);
  }

  public void useServerWorlds() {
    if (generatedWorlds.isEmpty()) return;
    List<World> previousGameWorlds = getGameWorlds();
    generatedWorlds.clear();
    updateGameWorlds();
    moveOutOfFormerGameWorlds(previousGameWorlds);
  }

  /**
   * Null while {@code world} is still a game world.
   */
  @Nullable
  public World getReplacement(@NotNull World world) {
    if (world != overworld && world != nether && world != end) return null;
    return generatedWorlds.get(world.getEnvironment());
  }

  @NotNull
  private World createGeneratedWorld(@NotNull String name, @NotNull Environment environment, @NotNull ChunkGenerator generator) {
    World world = new WorldCreator(name).environment(environment).generator(generator).seed(overworld.getSeed()).createWorld();
    if (world == null) throw new IllegalStateException("Could not create world " + name);

    World template = getServerWorld(environment);
    if (template != null) {
      world.setDifficulty(template.getDifficulty());
      // The registry also holds rules of disabled experiments, which a world rejects
      for (String ruleName : template.getGameRules()) {
        GameRule<?> rule = GameRule.getByName(ruleName);
        if (rule != null) copyGameRule(rule, template, world);
      }
    }
    Challenges.getInstance().getChallengeTimer().applyPauseRules(world);
    return world;
  }

  private static <T> void copyGameRule(@NotNull GameRule<T> rule, @NotNull World from, @NotNull World to) {
    T value = from.getGameRuleValue(rule);
    if (value != null) to.setGameRule(rule, value);
  }

  private void moveOutOfFormerGameWorlds(@NotNull List<World> previousGameWorlds) {
    Challenges.getInstance().getPlayerManager().forEachAudience(player -> {
      World world = player.getWorld();
      if (!previousGameWorlds.contains(world) || gameWorlds.contains(world)) return;

      World target = getWorld(world.getEnvironment());
      if (target != null) player.teleportAsync(BlockUtils.getCenteredSpawn(target));
    });
  }

  @NotNull
  public static String getGeneratedWorldName(@NotNull String namePrefix, @NotNull Environment environment) {
    switch (environment) {
      case NETHER:
        return namePrefix + "_nether";
      case THE_END:
        return namePrefix + "_the_end";
      default:
        return namePrefix;
    }
  }

  public World getWorld(@Nullable Environment environment) {
    if (environment == null) return null;
    World generated = generatedWorlds.get(environment);
    return generated != null ? generated : getServerWorld(environment);
  }

  public World getServerWorld(@NotNull Environment environment) {
    switch (environment) {
      case NORMAL:
        return overworld;
      case NETHER:
        return nether;
      case THE_END:
        return end;
      case CUSTOM:
        return Challenges.getInstance().getWorldManager().getExtraWorld();
    }
    return null;
  }

  public List<String> getCustomGeneratedGameWorlds() {
    return Collections.singletonList("void");
  }

  public List<World> getGameWorlds() {
    return gameWorlds;
  }

  private void updateGameWorlds() {
    List<World> worlds = new ArrayList<>(DIMENSIONS.length);
    for (Environment environment : DIMENSIONS) {
      World world = getWorld(environment);
      if (world != null) worlds.add(world);
    }
    gameWorlds = Collections.unmodifiableList(worlds);
  }

}
