package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForceTargetTest {

  @Test
  void targetsAreEqualByTypeAndValue() {
    assertEquals(new HeightTarget(64), new HeightTarget(64));
    assertEquals(new HeightTarget(64).hashCode(), new HeightTarget(64).hashCode());
    assertNotEquals(new HeightTarget(64), new HeightTarget(65));
    assertNotEquals(new HeightTarget(5), new DamageTarget(5));
  }

}
