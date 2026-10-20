package net.cyperia.challenges.plugin.management.scheduler.timer;

import org.junit.jupiter.api.Test;

import static net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer.TimerActionBarUpdateScheduler.isDue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChallengeTimerActionBarDueTest {

  @Test
  void notReduced_alwaysDue() {
    assertTrue(isDue(false, 0));
    assertTrue(isDue(false, 1));
    assertTrue(isDue(false, 19));
  }

  @Test
  void reduced_dueOnlyEveryTwentyTicks() {
    assertTrue(isDue(true, 0));
    assertTrue(isDue(true, 20));
    assertTrue(isDue(true, 40));
    assertFalse(isDue(true, 1));
    assertFalse(isDue(true, 19));
    assertFalse(isDue(true, 21));
  }

}
