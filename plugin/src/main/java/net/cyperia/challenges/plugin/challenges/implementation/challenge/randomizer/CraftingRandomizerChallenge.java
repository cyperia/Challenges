package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CraftingRandomizerChallenge extends RandomizerSetting {

  protected final Map<Material, ItemStack> randomization = new LinkedHashMap<>();

  @Override
  protected void reloadRandomization() {
    randomization.clear();

    // the first recipe of each result decides the amount it is crafted in
    Map<Material, ItemStack> results = new LinkedHashMap<>();
    Bukkit.recipeIterator().forEachRemaining(recipe -> {
      if (!(recipe instanceof CraftingRecipe)) return;
      ItemStack result = recipe.getResult();
      if (result.getType().isAir()) return;
      results.putIfAbsent(result.getType(), result);
    });

    List<Material> from = new ArrayList<>(results.keySet());
    from.sort(Comparator.naturalOrder());
    List<ItemStack> to = new ArrayList<>(from.size());
    from.forEach(material -> to.add(results.get(material)));
    random.shuffle(to);

    for (int i = 0; i < from.size(); i++) {
      randomization.put(from.get(i), to.get(i));
    }
  }

  @NotNull
  @Override
  public List<Pairing> getPairings() {
    return randomization.entrySet().stream().map(entry -> new Pairing(entry.getKey(), List.of(entry.getValue().getType()))).toList();
  }

  @Override
  protected void onDisable() {
    randomization.clear();
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onCraftItem(@NotNull CraftItemEvent event) {
    if (!isEnabled()) return;
    ItemStack item = event.getCurrentItem();
    if (item == null) return;
    ItemStack result = randomization.get(item.getType());
    if (result == null) return;
    event.setCurrentItem(result.clone());
  }

}
