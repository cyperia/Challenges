package net.cyperia.challenges.plugin.management.randomizer;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// the cheapest way to every item, counting each craft, smelt, broken block and killed mob as one step
public final class ObtainChain {

  private static final int UNREACHABLE = Integer.MAX_VALUE;

  private final Map<Material, Integer> cost = new HashMap<>();
  private final Map<Material, Source> cheapest = new HashMap<>();

  public ObtainChain(@NotNull Collection<? extends Source> sources) {
    // costs only ever drop, so this settles after about as many passes as the longest chain is deep
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Source source : sources) {
        int sourceCost = costOf(source);
        if (sourceCost == UNREACHABLE) continue;
        for (Material output : source.outputs()) {
          if (sourceCost >= cost.getOrDefault(output, UNREACHABLE)) continue;
          cost.put(output, sourceCost);
          cheapest.put(output, source);
          changed = true;
        }
      }
    }
  }

  @Nullable
  public Source getSource(@NotNull Material item) {
    return cheapest.get(item);
  }

  @NotNull
  public Material getCheapestOption(@NotNull Ingredient ingredient) {
    Material cheapestOption = ingredient.options().get(0);
    for (Material option : ingredient.options()) {
      if (cost.getOrDefault(option, UNREACHABLE) < cost.getOrDefault(cheapestOption, UNREACHABLE)) cheapestOption = option;
    }
    return cheapestOption;
  }

  private int costOf(@NotNull Source source) {
    int total = 1;
    for (Ingredient ingredient : source.ingredients()) {
      int ingredientCost = cost.getOrDefault(getCheapestOption(ingredient), UNREACHABLE);
      if (ingredientCost == UNREACHABLE) return UNREACHABLE;
      total += ingredientCost;
    }
    return total;
  }

  public sealed interface Source {

    @NotNull
    Collection<Material> outputs();

    @NotNull
    default List<Ingredient> ingredients() {
      return List.of();
    }

  }

  public record Ingredient(@NotNull List<Material> options, int count) {
  }

  // recipeOf is the result the recipe has in vanilla, set when the crafting randomizer changed it
  public record Craft(@NotNull Material output, @Nullable Material recipeOf, @NotNull List<Ingredient> ingredients) implements Source {

    @NotNull
    @Override
    public Collection<Material> outputs() {
      return List.of(output);
    }

  }

  public record Smelt(@NotNull Material output, @NotNull Ingredient input) implements Source {

    @NotNull
    @Override
    public Collection<Material> outputs() {
      return List.of(output);
    }

    @NotNull
    @Override
    public List<Ingredient> ingredients() {
      return List.of(input);
    }

  }

  public record Break(@NotNull Material block, @NotNull Collection<Material> outputs) implements Source {
  }

  // spawnsInsteadOf is set when the mob randomizer makes the mob appear in place of another one
  public record Kill(@NotNull EntityType mob, @Nullable EntityType spawnsInsteadOf, @NotNull Collection<Material> outputs) implements Source {
  }

}
