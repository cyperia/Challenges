package net.cyperia.challenges.plugin.challenges.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueUnitStepsTest {

  @Test
  void stepsPickTheirQuantityByPosition() {
    ValueUnit unit = ValueUnit.INTERVAL_SECONDS.steps(15, 30, 45, 60, 90, 120);
    assertEquals(15, unit.quantityOf(1));
    assertEquals(90, unit.quantityOf(5));
    assertEquals(120, unit.quantityOf(6));
    assertTrue(unit.isTime());
    assertFalse(unit.isLinear());
  }

  @Test
  void stepsAreScaledByTheUnderlyingUnit() {
    assertEquals(120, ValueUnit.INTERVAL_MINUTES.steps(1, 2).quantityOf(2));
    assertEquals(300, ValueUnit.INTERVAL_SECONDS.steps(1, 5).scaled(60).quantityOf(2));
  }

  @Test
  void scaledUnitsStayLinear() {
    assertEquals(300, ValueUnit.INTERVAL_MINUTES.quantityOf(5));
    assertTrue(ValueUnit.INTERVAL_MINUTES.isLinear());
  }

}
