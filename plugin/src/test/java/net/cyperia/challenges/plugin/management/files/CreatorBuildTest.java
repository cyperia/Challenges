package net.cyperia.challenges.plugin.management.files;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreatorBuildTest {

  private static InputStream text(String text) {
    return new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1));
  }

  @Test
  void readsTheCreatorAndTheBuild() throws IOException {
    assertEquals(new CreatorBuild("SouFake", "0a1b2c3d4e5f"), CreatorBuild.parse(text("creator=SouFake\nbuild=0a1b2c3d4e5f\nissued=2026-10-21\n")));
  }

  @Test
  void ignoresIncompleteMarks() throws IOException {
    assertNull(CreatorBuild.parse(text("creator=SouFake\n")));
    assertNull(CreatorBuild.parse(text("")));
  }

  @Test
  void aRegularBuildHasNoMarks() {
    assertNull(CreatorBuild.read(new ClassLoader(null) {}));
  }

}
