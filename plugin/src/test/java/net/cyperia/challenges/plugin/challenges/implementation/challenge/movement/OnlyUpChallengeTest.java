package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.implementation.challenge.movement.OnlyUpChallenge.Verdict;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OnlyUpChallengeTest {

  @Test
  void theFirstHeightIsTheStart() {
    assertEquals(Verdict.RAISE, OnlyUpChallenge.check(null, -58));
  }

  @Test
  void standingHigherRaisesTheHeight() {
    assertEquals(Verdict.RAISE, OnlyUpChallenge.check(-58, -57));
  }

  @Test
  void standingOnTheSameHeightIsFine() {
    assertEquals(Verdict.KEEP, OnlyUpChallenge.check(12, 12));
  }

  @Test
  void standingLowerFails() {
    assertEquals(Verdict.FAIL, OnlyUpChallenge.check(12, 11));
  }

}
