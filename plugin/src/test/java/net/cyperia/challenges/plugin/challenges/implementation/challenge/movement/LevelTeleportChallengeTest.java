package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelTeleportChallengeTest {

  @Test
  void everyLevelGainedIsOnePearl() {
    assertEquals(1, LevelTeleportChallenge.pearlsFor(0, 1));
    assertEquals(3, LevelTeleportChallenge.pearlsFor(4, 7));
  }

  @Test
  void losingLevelsGivesNothing() {
    assertEquals(0, LevelTeleportChallenge.pearlsFor(30, 27));
    assertEquals(0, LevelTeleportChallenge.pearlsFor(5, 0));
  }

}
