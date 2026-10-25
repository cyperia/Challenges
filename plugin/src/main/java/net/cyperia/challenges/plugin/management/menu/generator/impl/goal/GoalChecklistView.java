package net.cyperia.challenges.plugin.management.menu.generator.impl.goal;

import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

record GoalChecklistView(@NotNull Filter filter, @NotNull Sort sort, boolean descending) {

  static final GoalChecklistView DEFAULT = new GoalChecklistView(Filter.ALL, Sort.DEFAULT, false);

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

  /**
   * @param names name of a finder, {@code null} if unknown
   */
  @NotNull
  List<ChecklistEntry> apply(@NotNull List<ChecklistEntry> entries, @NotNull Function<UUID, String> names) {
    // Entries without a time or finder stay at the end in both directions
    Comparator<ChecklistEntry> order = switch (sort) {
      // stable, so each goal's own order holds within missing and done
      case DEFAULT -> descending ? Comparator.comparing(ChecklistEntry::done).reversed() : Comparator.comparing(ChecklistEntry::done);
      case TIME -> Comparator.comparing(ChecklistEntry::foundAt, Comparator.nullsLast(direction(Comparator.<Long>naturalOrder())));
      case FINDER -> Comparator.comparing(entry -> firstFinder(entry, names), Comparator.nullsLast(direction(String.CASE_INSENSITIVE_ORDER)));
    };
    return entries.stream()
      .filter(entry -> switch (filter) {
        case ALL -> true;
        case MISSING -> !entry.done();
        case FOUND -> entry.done();
      })
      .sorted(order)
      .toList();
  }

  @NotNull
  private <T> Comparator<T> direction(@NotNull Comparator<T> ascending) {
    return descending ? ascending.reversed() : ascending;
  }

  @Nullable
  private static String firstFinder(@NotNull ChecklistEntry entry, @NotNull Function<UUID, String> names) {
    return entry.finders().stream().map(names).filter(name -> name != null).min(String.CASE_INSENSITIVE_ORDER).orElse(null);
  }

}
