package net.cyperia.challenges.plugin.management.menu.generator.impl.goal;

import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistCategory;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistEntry;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoalChecklistViewTest {

  private static final UUID ANNA = UUID.randomUUID();
  private static final UUID BEN = UUID.randomUUID();
  private static final Map<UUID, String> NAMES = Map.of(ANNA, "anna", BEN, "Ben");

  private static final ChecklistEntry MISSING = entry("missing", false, List.of(), null);
  private static final ChecklistEntry EARLY_BY_BEN = entry("early", true, List.of(BEN), 10L);
  private static final ChecklistEntry LATE_BY_ANNA = entry("late", true, List.of(ANNA), 20L);
  private static final ChecklistEntry SKIPPED = entry("skipped", true, List.of(), 15L);
  private static final List<ChecklistEntry> ENTRIES = List.of(EARLY_BY_BEN, MISSING, LATE_BY_ANNA, SKIPPED);

  private static ChecklistEntry entry(String name, boolean done, List<UUID> finders, Long foundAt) {
    return new ChecklistEntry(name, Mockito.mock(ItemStack.class), done, finders, foundAt);
  }

  private static List<ChecklistEntry> apply(GoalChecklistView.Filter filter, GoalChecklistView.Sort sort, boolean descending) {
    return new GoalChecklistView(filter, sort, descending, GoalChecklistView.ALL_CATEGORIES).apply(ENTRIES, NAMES::get);
  }

  @Test
  void defaultKeepsTheGoalOrderWithMissingFirst() {
    assertEquals(List.of(MISSING, EARLY_BY_BEN, LATE_BY_ANNA, SKIPPED),
      apply(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.DEFAULT, false));
    assertEquals(List.of(EARLY_BY_BEN, LATE_BY_ANNA, SKIPPED, MISSING),
      apply(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.DEFAULT, true));
  }

  @Test
  void filtersMissingAndFound() {
    assertEquals(List.of(MISSING), apply(GoalChecklistView.Filter.MISSING, GoalChecklistView.Sort.DEFAULT, false));
    assertEquals(List.of(EARLY_BY_BEN, LATE_BY_ANNA, SKIPPED), apply(GoalChecklistView.Filter.FOUND, GoalChecklistView.Sort.DEFAULT, false));
  }

  @Test
  void sortsByTimeWithEntriesWithoutTimeLastInBothDirections() {
    assertEquals(List.of(EARLY_BY_BEN, SKIPPED, LATE_BY_ANNA, MISSING),
      apply(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.TIME, false));
    assertEquals(List.of(LATE_BY_ANNA, SKIPPED, EARLY_BY_BEN, MISSING),
      apply(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.TIME, true));
  }

  @Test
  void sortsByFinderNameIgnoringCase() {
    assertEquals(List.of(LATE_BY_ANNA, EARLY_BY_BEN, MISSING, SKIPPED),
      apply(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.FINDER, false));
  }

  @Test
  void sortsByFinderDescendingWithEntriesWithoutFinderLast() {
    assertEquals(List.of(EARLY_BY_BEN, LATE_BY_ANNA, MISSING, SKIPPED),
      apply(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.FINDER, true));
  }

  @Test
  void usesTheAlphabeticallyFirstOfSeveralFinders() {
    ChecklistEntry shared = entry("shared", true, List.of(BEN, ANNA), 5L);
    ChecklistEntry onlyBen = entry("onlyBen", true, List.of(BEN), 6L);

    assertEquals(List.of(shared, onlyBen),
      new GoalChecklistView(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.FINDER, false, GoalChecklistView.ALL_CATEGORIES)
        .apply(List.of(onlyBen, shared), NAMES::get));
  }

  @Test
  void resolvesEachFinderNameAtMostOnce() {
    Map<UUID, Integer> calls = new HashMap<>();
    List<ChecklistEntry> many = new ArrayList<>();
    for (int i = 0; i < 50; i++) many.add(entry("e" + i, true, List.of(i % 2 == 0 ? ANNA : BEN), (long) i));
    many.add(MISSING);

    new GoalChecklistView(GoalChecklistView.Filter.ALL, GoalChecklistView.Sort.FINDER, false, GoalChecklistView.ALL_CATEGORIES)
      .apply(many, uuid -> {
        calls.merge(uuid, 1, Integer::sum);
        return NAMES.get(uuid);
      });

    assertEquals(Map.of(ANNA, 1, BEN, 1), calls);
  }

  @Test
  void doesNotResolveNamesOfFilteredOutEntries() {
    List<UUID> asked = new ArrayList<>();

    new GoalChecklistView(GoalChecklistView.Filter.MISSING, GoalChecklistView.Sort.FINDER, false, GoalChecklistView.ALL_CATEGORIES)
      .apply(ENTRIES, uuid -> {
        asked.add(uuid);
        return NAMES.get(uuid);
      });

    assertEquals(List.of(), asked);
  }

  private static final List<ChecklistCategory> CATEGORIES = List.of(
    new ChecklistCategory(Mockito.mock(LocalizableMessage.class), Mockito.mock(ItemStack.class), List.of(MISSING)),
    new ChecklistCategory(Mockito.mock(LocalizableMessage.class), Mockito.mock(ItemStack.class), List.of(EARLY_BY_BEN, SKIPPED)));

  @Test
  void aCategoryShowsOnlyItsEntries() {
    GoalChecklistView view = GoalChecklistView.DEFAULT.toggleCategory(1);

    assertEquals(List.of(EARLY_BY_BEN, SKIPPED), view.select(ENTRIES, CATEGORIES));
  }

  @Test
  void choosingTheShownCategoryAgainShowsEverything() {
    GoalChecklistView view = GoalChecklistView.DEFAULT.toggleCategory(1).toggleCategory(1);

    assertEquals(ENTRIES, view.select(ENTRIES, CATEGORIES));
  }

  @Test
  void aCategoryTheGoalDoesNotHaveShowsEverything() {
    GoalChecklistView view = GoalChecklistView.DEFAULT.toggleCategory(1);

    assertEquals(ENTRIES, view.select(ENTRIES, List.of()));
  }

}
