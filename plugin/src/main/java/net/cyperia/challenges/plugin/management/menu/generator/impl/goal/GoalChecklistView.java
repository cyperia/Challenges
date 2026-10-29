package net.cyperia.challenges.plugin.management.menu.generator.impl.goal;

import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistCategory;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * @param category index into the goal's checklist categories, {@link #ALL_CATEGORIES} to show every entry
 */
record GoalChecklistView(@NotNull Filter filter, @NotNull Sort sort, boolean descending, int category) {

  static final int ALL_CATEGORIES = -1;
  static final GoalChecklistView DEFAULT = new GoalChecklistView(Filter.ALL, Sort.DEFAULT, false, ALL_CATEGORIES);

  enum Filter {
    ALL, MISSING, FOUND;

    @NotNull
    Filter next() {
      return values()[(ordinal() + 1) % values().length];
    }
  }

  enum Sort {
    DEFAULT, TIME, FINDER;

    @NotNull
    Sort next() {
      return values()[(ordinal() + 1) % values().length];
    }
  }

  // A category the goal no longer has, like after switching goals, shows everything
  @NotNull
  List<ChecklistEntry> select(@NotNull List<ChecklistEntry> all, @NotNull List<ChecklistCategory> categories) {
    return category >= 0 && category < categories.size() ? categories.get(category).entries() : all;
  }

  @NotNull
  GoalChecklistView withFilter(@NotNull Filter filter) {
    return new GoalChecklistView(filter, sort, descending, category);
  }

  @NotNull
  GoalChecklistView withSort(@NotNull Sort sort, boolean descending) {
    return new GoalChecklistView(filter, sort, descending, category);
  }

  // Choosing the shown category again goes back to every entry
  @NotNull
  GoalChecklistView toggleCategory(int category) {
    return new GoalChecklistView(filter, sort, descending, this.category == category ? ALL_CATEGORIES : category);
  }

  /**
   * @param names name of a finder, {@code null} if unknown; called at most once per finder
   */
  @NotNull
  List<ChecklistEntry> apply(@NotNull List<ChecklistEntry> entries, @NotNull Function<UUID, String> names) {
    List<ChecklistEntry> shown = entries.stream()
      .filter(entry -> switch (filter) {
        case ALL -> true;
        case MISSING -> !entry.done();
        case FOUND -> entry.done();
      })
      .toList();
    // Entries without a time or finder stay at the end in both directions
    Comparator<ChecklistEntry> order = switch (sort) {
      // stable, so each goal's own order holds within missing and done
      case DEFAULT -> descending ? Comparator.comparing(ChecklistEntry::done).reversed() : Comparator.comparing(ChecklistEntry::done);
      case TIME -> Comparator.comparing(ChecklistEntry::foundAt, Comparator.nullsLast(direction(Comparator.<Long>naturalOrder())));
      case FINDER -> {
        // Name lookups hit the server's player cache, so each entry's key is resolved once, not once per comparison
        Map<ChecklistEntry, String> keys = firstFinders(shown, names);
        yield Comparator.comparing(keys::get, Comparator.nullsLast(direction(String.CASE_INSENSITIVE_ORDER)));
      }
    };
    return shown.stream().sorted(order).toList();
  }

  @NotNull
  private <T> Comparator<T> direction(@NotNull Comparator<T> ascending) {
    return descending ? ascending.reversed() : ascending;
  }

  @NotNull
  private static Map<ChecklistEntry, String> firstFinders(@NotNull List<ChecklistEntry> entries, @NotNull Function<UUID, String> names) {
    Map<UUID, String> resolved = new HashMap<>();
    Map<ChecklistEntry, String> keys = new IdentityHashMap<>();
    for (ChecklistEntry entry : entries) {
      String first = null;
      for (UUID finder : entry.finders()) {
        if (!resolved.containsKey(finder)) resolved.put(finder, names.apply(finder));
        String name = resolved.get(finder);
        if (name != null && (first == null || String.CASE_INSENSITIVE_ORDER.compare(name, first) < 0)) first = name;
      }
      keys.put(entry, first);
    }
    return keys;
  }

}
