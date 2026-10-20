package net.cyperia.challenges.plugin.management.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * One arrangement of the hotbar, mapping hotbar slots (0-8) to {@link HotbarItem#getId() item ids}.
 */
public record HotbarLayout(@NotNull Map<Integer, String> slots) {

  private static final Comparator<Fit> BEST_FIT = Comparator
    .comparingInt(Fit::shown).reversed()
    .thenComparingInt(Fit::missing);

  public HotbarLayout {
    slots = Map.copyOf(slots);
  }

  /**
   * Picks the layout showing the most of the available items. Between layouts showing equally many,
   * the one referencing the fewest unavailable items wins, then the one declared first. Unavailable
   * items of the chosen layout are left out.
   *
   * @return the best layout, or {@code null} if none shows any of the available items
   */
  @Nullable
  public static HotbarLayout select(@NotNull Collection<HotbarLayout> layouts, @NotNull Set<String> available) {
    return layouts.stream()
      .map(layout -> layout.fit(available))
      .filter(fit -> fit.shown() > 0)
      .min(BEST_FIT) // keeps the first of equal elements
      .map(Fit::layout)
      .orElse(null);
  }

  @NotNull
  private Fit fit(@NotNull Set<String> available) {
    int shown = (int) slots.values().stream().filter(available::contains).count();
    return new Fit(this, shown, slots.size() - shown);
  }

  private record Fit(@NotNull HotbarLayout layout, int shown, int missing) {
  }

}
