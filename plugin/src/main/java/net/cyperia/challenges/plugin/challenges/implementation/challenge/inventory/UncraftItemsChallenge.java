package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UncraftItemsChallenge extends TimedChallenge {

  public UncraftItemsChallenge() {
    super(5, 60, 20);
    setValueUnit(ValueUnit.INTERVAL_SECONDS);
  }

  public static void uncraftInventory(@NotNull Player player) {

    PlayerInventory inventory = player.getInventory();

    List<ItemStack> itemsToAdd = new ArrayList<>();

    for (int slot = 0; slot < inventory.getContents().length; slot++) {
      ItemStack item = inventory.getItem(slot);
      if (item == null) continue;
      List<Recipe> recipes = Bukkit.getRecipesFor(new ItemStack(item.getType()));
      if (recipes.isEmpty()) continue;

      Recipe recipe = null;
      for (Recipe currentRecipe : recipes) {

        if (canCraft(recipes, item.getType())) {
          continue;
        }

        recipe = currentRecipe;
      }

      if (recipe == null) continue;

      ItemStack[] ingredients = getIngredientsOfRecipe(recipe).toArray(new ItemStack[0]);

      for (int i = 0; i < item.getAmount() / recipe.getResult().getAmount(); i++) {
        for (ItemStack ingredient : ingredients) {
          if (ingredient == null) {
            continue;
          }
          itemsToAdd.add(ingredient);
        }
      }
      inventory.setItem(slot, null);
    }

    itemsToAdd.forEach(itemStack -> InventoryUtils.giveItem(player, itemStack));

  }

  private static boolean canCraft(List<Recipe> recipes, Material material) {

    for (Recipe recipe : recipes) {
      for (ItemStack ingredient : getIngredientsOfRecipe(recipe)) {
        if (ingredient == null) continue;

        List<Recipe> ingredientRecipes = Bukkit.getRecipesFor(ingredient);

        for (Recipe ingredientRecipe : ingredientRecipes) {
          for (ItemStack itemStack : getIngredientsOfRecipe(ingredientRecipe)) {
            if (itemStack == null) continue;
            if (itemStack.getType() == material) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private static List<ItemStack> getIngredientsOfRecipe(@NotNull Recipe recipe) {
    List<ItemStack> ingredients = new ArrayList<>();
    if (recipe instanceof ShapedRecipe) {
      ShapedRecipe shaped = (ShapedRecipe) recipe;
      ingredients.addAll(shaped.getIngredientMap().values());
    } else if (recipe instanceof ShapelessRecipe) {
      ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
      ingredients.addAll(shapeless.getIngredientList());
    } else if (recipe instanceof FurnaceRecipe) {
      FurnaceRecipe furnace = (FurnaceRecipe) recipe;
      ingredients.add(furnace.getInput());
    } else if (recipe instanceof SmithingRecipe) {
      SmithingRecipe smithing = (SmithingRecipe) recipe;
      ingredients.add(Objects.requireNonNull(smithing.getBase()).getItemStack());
      ingredients.add(Objects.requireNonNull(smithing.getAddition()).getItemStack());
    }

    return ingredients;
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();
    broadcastFiltered(UncraftItemsChallenge::uncraftInventory);
  }

}
