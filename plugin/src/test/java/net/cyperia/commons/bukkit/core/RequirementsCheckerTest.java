package net.cyperia.commons.bukkit.core;

import net.cyperia.commons.common.version.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementsCheckerTest {

  private static final Version REQUIRED = Version.parse("26.3");

  @Test
  void supportsEveryPatchOfTheRequiredLine() {
    assertTrue(RequirementsChecker.supports(REQUIRED, Version.parse("26.3")));
    assertTrue(RequirementsChecker.supports(REQUIRED, Version.parse("26.3.2")));
  }

  @Test
  void doesNotSupportOlderOrNewerLines() {
    assertFalse(RequirementsChecker.supports(REQUIRED, Version.parse("26.2")));
    assertFalse(RequirementsChecker.supports(REQUIRED, Version.parse("26.4")));
    assertFalse(RequirementsChecker.supports(REQUIRED, Version.parse("1.21.11")));
    assertFalse(RequirementsChecker.supports(REQUIRED, Version.parse("27.3")));
  }

}
