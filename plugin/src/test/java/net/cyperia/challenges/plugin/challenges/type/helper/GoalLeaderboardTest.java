package net.cyperia.challenges.plugin.challenges.type.helper;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GoalLeaderboardTest {

  @Test
  void aViewerAtTheTopSeesTheWholeRun() {
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7), GoalLeaderboard.sidebarRows(20, 2, 5, 1));
  }

  @Test
  void anUnrankedViewerSeesTheTop() {
    assertEquals(List.of(0, 1, 2), GoalLeaderboard.sidebarRows(3, -1, 5, 1));
  }

  @Test
  void aViewerJustBelowTheTopGetsNoGapOfOneLine() {
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7), GoalLeaderboard.sidebarRows(20, 6, 5, 1));
  }

  @Test
  void aViewerFurtherDownSeesTheTopAndTheirNeighbours() {
    assertEquals(List.of(0, 1, 2, 3, 4, SKIPPED, 11, 12, 13), GoalLeaderboard.sidebarRows(20, 12, 5, 1));
  }

  @Test
  void theLastPlaceHasNobodyBelow() {
    assertEquals(List.of(0, 1, 2, 3, 4, SKIPPED, 18, 19), GoalLeaderboard.sidebarRows(20, 19, 5, 1));
  }

}
