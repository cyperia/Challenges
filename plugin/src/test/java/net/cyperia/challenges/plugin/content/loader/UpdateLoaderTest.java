package net.cyperia.challenges.plugin.content.loader;

import net.cyperia.commons.common.version.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateLoaderTest {

  @Test
  void picksTheNewestReleaseAndSkipsBetas() {
    String json = """
      [
        {"version_number": "3.2", "version_type": "beta"},
        {"version_number": "3.0", "version_type": "release"},
        {"version_number": "3.1", "version_type": "release"}
      ]""";
    assertEquals("3.1", UpdateLoader.newestRelease(json).format());
  }

  @Test
  void noReleaseForThisMinecraftVersionMeansNoUpdate() {
    assertNull(UpdateLoader.newestRelease("[]"));
  }

  @Test
  void aNewerReleaseIsAnUpdate() {
    Version newest = UpdateLoader.newestRelease("[{\"version_number\": \"3.0.1\", \"version_type\": \"release\"}]");
    assertEquals(true, newest.isNewerThan(Version.parse("3.0")));
  }

}
