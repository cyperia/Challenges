package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NoSameHeartsChallengeTest {

  @Test
  void aStartedHalfHeartCountsAsOne() {
    assertEquals(7, NoSameHeartsChallenge.halfHearts(6.3));
    assertEquals(NoSameHeartsChallenge.halfHearts(6.1), NoSameHeartsChallenge.halfHearts(6.9));
  }

  @Test
  void wholeHalfHeartsStayApart() {
    assertNotEquals(NoSameHeartsChallenge.halfHearts(6), NoSameHeartsChallenge.halfHearts(7));
  }

  @Test
  void playersStartTwoHeartsApart() {
    assertEquals(20, NoSameHeartsChallenge.startHealth(20, 0));
    assertEquals(16, NoSameHeartsChallenge.startHealth(20, 1));
    assertEquals(12, NoSameHeartsChallenge.startHealth(20, 2));
  }

  @Test
  void theStartNeverKills() {
    assertEquals(1, NoSameHeartsChallenge.startHealth(20, 8));
  }

}
