package net.cyperia.challenges.plugin.management.menu.generator.impl.goal;

import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistEntry;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    return new GoalChecklistView(filter, sort, descending).apply(ENTRIES, NAMES::get);
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

}
