package net.cyperia.challenges.plugin.management.randomizer;

import net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer.*;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting.Pairing;
import net.cyperia.challenges.plugin.management.randomizer.ObtainChain.*;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// every way to get an item in the current game, with the enabled randomizers applied
public final class GameObtainSources {

  private GameObtainSources() {
  }

  @NotNull
  public static List<Source> collect() {
    List<Source> sources = new ArrayList<>();
    Set<Material> crafted = new HashSet<>();
    Map<Enum<?>, Enum<?>> crafting = mappingOf(CraftingRandomizerChallenge.class);

    Bukkit.recipeIterator().forEachRemaining(recipe -> {
      Material result = recipe.getResult().getType();
      if (result.isAir()) return;
      List<Ingredient> ingredients = ingredientsOf(recipe);
      if (ingredients == null || ingredients.isEmpty()) return;

      if (recipe instanceof CookingRecipe<?>) {
        sources.add(new Smelt(result, ingredients.get(0)));
      } else if (recipe instanceof CraftingRecipe) {
        crafted.add(result);
        Material output = (Material) crafting.getOrDefault(result, result);
        sources.add(new Craft(output, output == result ? null : result, ingredients));
      }
    });

    Map<Enum<?>, Enum<?>> blockSources = mappingOf(BlockRandomizerChallenge.class);
    Map<Enum<?>, List<? extends Enum<?>>> blockItems = listMappingOf(BlockItemRandomizerChallenge.class);
    for (Material block : ExperimentalUtils.getMaterials()) {
      // a block counts as generated in the world when nothing crafts it, which misses crafted blocks that also generate, like sandstone
      if (!block.isBlock() || !block.isItem() || !ItemUtils.isObtainableInSurvival(block) || crafted.contains(block)) continue;
      Collection<Material> drops = blockItems.containsKey(block)
        ? blockItems.get(block).stream().map(Material.class::cast).toList()
        : VanillaLoot.of((Material) blockSources.getOrDefault(block, block));
      if (!drops.isEmpty()) sources.add(new Break(block, drops));
    }

    Map<Enum<?>, Enum<?>> spawnsAs = mappingOf(MobRandomizerChallenge.class);
    Map<Enum<?>, Enum<?>> lootOf = mappingOf(EntityLootRandomizerChallenge.class);
    for (EntityType natural : RandomizerSetting.NATURAL_MOBS) {
      EntityType mob = (EntityType) spawnsAs.getOrDefault(natural, natural);
      Collection<Material> drops = VanillaLoot.of((EntityType) lootOf.getOrDefault(mob, mob));
      if (!drops.isEmpty()) sources.add(new Kill(mob, mob == natural ? null : natural, drops));
    }

    return sources;
  }

  @Nullable
  private static List<Ingredient> ingredientsOf(@NotNull Recipe recipe) {
    List<RecipeChoice> choices = new ArrayList<>();
    switch (recipe) {
      case ShapedRecipe shaped -> {
        for (String row : shaped.getShape()) {
          for (char key : row.toCharArray()) {
            RecipeChoice choice = shaped.getChoiceMap().get(key);
            if (choice != null) choices.add(choice);
          }
        }
      }
      case ShapelessRecipe shapeless -> choices.addAll(shapeless.getChoiceList());
      case TransmuteRecipe transmute -> {
        choices.add(transmute.getInput());
        choices.add(transmute.getMaterial());
      }
      case CookingRecipe<?> cooking -> choices.add(cooking.getInputChoice());
      default -> {
        return null;
      }
    }

    Map<List<Material>, Integer> counts = new LinkedHashMap<>();
    for (RecipeChoice choice : choices) {
      List<Material> options = optionsOf(choice);
      if (options.isEmpty()) return null;
      counts.merge(options, 1, Integer::sum);
    }
    List<Ingredient> ingredients = new ArrayList<>(counts.size());
    counts.forEach((options, count) -> ingredients.add(new Ingredient(options, count)));
    return ingredients;
  }

  @NotNull
  private static List<Material> optionsOf(@NotNull RecipeChoice choice) {
    return switch (choice) {
      case RecipeChoice.MaterialChoice materials -> materials.getChoices();
      case RecipeChoice.ExactChoice exact -> exact.getChoices().stream().map(ItemStack::getType).distinct().toList();
      default -> List.of();
    };
  }

  // the randomizer's pairings as from -> to, empty while it is disabled
  @NotNull
  private static Map<Enum<?>, Enum<?>> mappingOf(@NotNull Class<? extends RandomizerSetting> type) {
    Map<Enum<?>, Enum<?>> mapping = new HashMap<>();
    listMappingOf(type).forEach((from, to) -> mapping.put(from, to.get(0)));
    return mapping;
  }

  @NotNull
  private static Map<Enum<?>, List<? extends Enum<?>>> listMappingOf(@NotNull Class<? extends RandomizerSetting> type) {
    Map<Enum<?>, List<? extends Enum<?>>> mapping = new HashMap<>();
    RandomizerSetting randomizer = AbstractChallenge.getFirstInstance(type);
    if (!randomizer.isEnabled()) return mapping;
    for (Pairing pairing : randomizer.getPairings()) mapping.put(pairing.from(), pairing.to());
    return mapping;
  }

}
