package net.cyperia.challenges.plugin.challenges.type.abstraction;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionGoalTest {

  private static final UUID ALICE = UUID.randomUUID();
  private static final UUID BOB = UUID.randomUUID();

  @Test
  void collectorsAreEveryoneWhoHasTheEntry() {
    Map<UUID, List<String>> collections = Map.of(
      ALICE, List.of("OAK_LOG", "BIRCH_LOG"),
      BOB, List.of("OAK_LOG")
    );

    assertEquals(2, CollectionGoal.getCollectors(collections, "OAK_LOG").size());
    assertEquals(List.of(ALICE), CollectionGoal.getCollectors(collections, "BIRCH_LOG"));
    assertEquals(List.of(), CollectionGoal.getCollectors(collections, "SPRUCE_LOG"));
  }

}
