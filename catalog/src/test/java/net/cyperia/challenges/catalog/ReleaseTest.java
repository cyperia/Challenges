package net.cyperia.challenges.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseTest {

  @Test
  void theBundledReleaseIsThePluginVersion() {
    assertTrue(Release.CURRENT.matches("\\d+(\\.\\d+)*.*"), Release.CURRENT);
  }

  @Test
  void aVersionReachesEveryReleaseUpToItself() {
    assertTrue(Release.reached("3.0", "3.0"));
    assertTrue(Release.reached("3.0", "3.0.0-SNAPSHOT"));
    assertTrue(Release.reached("3.1", "3.0.9"));
    assertFalse(Release.reached("2.3.2", "3.0"));
    assertTrue(Release.reached("3.1", "3.1.1"));
    assertFalse(Release.reached("3.0", "3.1.1"));
    assertFalse(Release.reached(null, "3.0"));
  }

  @Test
  void betaWinsOverNewAndUpdated() {
    assertTrue(new Release(Release.CURRENT, Release.CURRENT, false).isNew());
    assertTrue(new Release(null, Release.CURRENT, false).isUpdated());
    Release beta = new Release(Release.CURRENT, Release.CURRENT, true);
    assertFalse(beta.isNew());
    assertFalse(beta.isUpdated());
    assertFalse(new Release("1.3", null, false).isNew());
  }

}
