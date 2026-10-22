package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeverFullHeartsChallengeTest {

  @Test
  void fullHealthIsFull() {
    assertTrue(NeverFullHeartsChallenge.isFull(20, 20));
    assertTrue(NeverFullHeartsChallenge.isFull(40, 40));
  }

  @Test
  void halfAHeartShortIsNotFull() {
    assertFalse(NeverFullHeartsChallenge.isFull(19, 20));
    assertFalse(NeverFullHeartsChallenge.isFull(19.9, 20));
  }

}
