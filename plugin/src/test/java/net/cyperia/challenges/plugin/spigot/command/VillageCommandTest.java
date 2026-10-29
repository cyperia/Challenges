package net.cyperia.challenges.plugin.spigot.command;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VillageCommandTest {

  private static final int[] RADII = {100, 250, 500};

  private final Queue<Runnable> ticks = new ArrayDeque<>();

  private void runAllTicks() {
    while (!ticks.isEmpty()) ticks.poll().run();
  }

  @Test
  void searchesNothingBeforeTheFirstTick() {
    List<Integer> searched = new ArrayList<>();
    VillageCommand.searchRings(RADII, 0, radius -> {
      searched.add(radius);
      return null;
    }, ticks::add, village -> { }, () -> { });
    assertTrue(searched.isEmpty());
  }

  @Test
  void widensOneRingPerTickUntilFound() {
    List<Integer> searched = new ArrayList<>();
    Location found = new Location(null, 1, 2, 3);
    List<Location> result = new ArrayList<>();
    VillageCommand.searchRings(RADII, 0, radius -> {
      searched.add(radius);
      return radius == 250 ? found : null;
    }, ticks::add, result::add, () -> result.add(null));

    ticks.poll().run();
    assertEquals(List.of(100), searched);
    ticks.poll().run();
    assertEquals(List.of(100, 250), searched);
    assertTrue(ticks.isEmpty());
    assertEquals(List.of(found), result);
  }

  @Test
  void reportsNoneAfterTheLastRing() {
    List<Integer> searched = new ArrayList<>();
    AtomicInteger none = new AtomicInteger();
    VillageCommand.searchRings(RADII, 0, radius -> {
      searched.add(radius);
      return null;
    }, ticks::add, village -> { throw new AssertionError(); }, none::incrementAndGet);

    runAllTicks();
    assertEquals(List.of(100, 250, 500), searched);
    assertEquals(1, none.get());
  }

}
