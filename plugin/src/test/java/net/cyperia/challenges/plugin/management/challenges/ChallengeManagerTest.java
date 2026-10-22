package net.cyperia.challenges.plugin.management.challenges;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.GsonDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChallengeManagerTest {

  @Test
  void findsSettingsSavedUnderAFallbackId() {
    Document config = new GsonDocument().set("preset-no-eating", new GsonDocument().set("enabled", true));
    assertEquals("preset-no-eating", ChallengeManager.savedName(config, "no-eating", List.of("preset-no-eating")));
  }

  @Test
  void prefersTheCurrentId() {
    Document config = new GsonDocument()
      .set("preset-no-eating", new GsonDocument().set("enabled", true))
      .set("no-eating", new GsonDocument().set("enabled", false));
    assertEquals("no-eating", ChallengeManager.savedName(config, "no-eating", List.of("preset-no-eating")));
  }

  @Test
  void nothingSaved() {
    assertNull(ChallengeManager.savedName(new GsonDocument(), "no-eating", List.of("preset-no-eating")));
  }

}
