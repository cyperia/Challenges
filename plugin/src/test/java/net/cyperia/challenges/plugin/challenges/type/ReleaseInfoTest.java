package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.catalog.Release;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReleaseInfoTest {

  @Test
  void newerSubSettingUpdatesTheSetting() {
    assertEquals(new Release("2.0", "3.1", false), ReleaseInfo.withSubSettings(new Release("2.0", null, false), "3.1"));
    assertEquals(new Release("2.0", "3.1", false), ReleaseInfo.withSubSettings(new Release("2.0", "3.0", false), "3.1"));
  }

  @Test
  void subSettingOfTheSameReleaseIsNoUpdate() {
    Release release = new Release("3.1", null, false);
    assertEquals(release, ReleaseInfo.withSubSettings(release, "3.1"));
  }

  @Test
  void laterOwnUpdateStays() {
    Release release = new Release("2.0", "3.2", false);
    assertEquals(release, ReleaseInfo.withSubSettings(release, "3.1"));
  }

  @Test
  void withoutVersionedSubSettingsTheReleaseStays() {
    Release release = new Release("2.0", null, true);
    assertEquals(release, ReleaseInfo.withSubSettings(release, null));
  }

}
