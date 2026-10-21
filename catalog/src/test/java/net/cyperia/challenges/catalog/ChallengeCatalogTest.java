package net.cyperia.challenges.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChallengeCatalogTest {

  private static final String IMPLEMENTATION = "net.cyperia.challenges.plugin.challenges.implementation.";

  private final ChallengeCatalog bundled = ChallengeCatalog.load();

  @Test
  void theBundledCatalogIsComplete() {
    assertSame(bundled, bundled.requireComplete());
    assertFalse(bundled.challenges().isEmpty());
    assertFalse(bundled.triggers().isEmpty());
    assertFalse(bundled.actions().isEmpty());
  }

  @Test
  void readsAChallengeWithItsClassAndRelease() {
    Challenge cutClean = bundled.challenge("cutclean").orElseThrow();
    assertEquals("cut-clean", cutClean.key());
    assertEquals("utility", cutClean.category());
    assertEquals(new Icon("IRON_AXE", null), cutClean.icon());
    assertEquals(new Definition.Implementation(IMPLEMENTATION + "setting.CutCleanSetting", null), cutClean.definition());
    assertEquals("2.0", cutClean.release().since());
    assertSame(cutClean, bundled.challengeOf(IMPLEMENTATION + "setting.CutCleanSetting").orElseThrow());
  }

  @Test
  void anEntryIsRegisteredUnlessItSaysOtherwise() {
    assertTrue(bundled.challenge("cutclean").orElseThrow().registered());
    assertFalse(bundled.challenge("damageteleport").orElseThrow().registered());
  }

  // Enabling one moves every player to the spawn of a new world, so a random pick would reset the run
  @Test
  void worldGeneratorsAreNeverPickedAtRandom() {
    List<Challenge> generators = bundled.challenges().stream().filter(challenge -> challenge.category().equals("world_generator")).toList();
    assertFalse(generators.isEmpty());
    generators.forEach(generator -> assertTrue(generator.excludeFromRandom(), generator.id()));
  }

  @Test
  void readsAPresetWithItsValue() {
    Definition.Preset preset = (Definition.Preset) bundled.challenge("preset-interval-border").orElseThrow().definition();
    assertEquals("interval", preset.trigger());
    assertEquals(Map.of("change", List.of("-1")), preset.actionSettings());
    assertEquals(new Definition.Preset.Value("INTERVAL_MINUTES", 1, 10, 1), preset.value());
  }

  @Test
  void readsRulesAndTintedIcons() {
    assertEquals(new Definition.DamageRule(List.of(Definition.DamageRule.EVERY_CAUSE)),
      bundled.challenge("damagerulenone").orElseThrow().definition());
    assertEquals(new Icon("POTION", 0x0000FF), bundled.challenge("damageruledrowning").orElseThrow().icon());
  }

  @Test
  void readsFallbackIdsOfTriggers() {
    assertEquals(List.of("intervall"), bundled.trigger("interval").orElseThrow().fallbackIds());
  }

  @Test
  void rejectsAnUnknownField(@TempDir Path dir) throws IOException {
    assertRejected(dir, """
      {"challenges": [{"id": "a", "key": "a", "category": "c", "icon": "STONE", "class": "A", "colour": "#FFFFFF"}]}
      """, "colour");
  }

  @Test
  void rejectsAnEntryWithTwoDefinitions(@TempDir Path dir) throws IOException {
    assertRejected(dir, """
      {"challenges": [{"id": "a", "key": "a", "category": "c", "icon": "STONE", "class": "A", "materialRule": ["STONE"]}]}
      """, "exactly one");
  }

  @Test
  void rejectsARequiredPluginOnAPreset(@TempDir Path dir) throws IOException {
    assertRejected(dir, """
      {"challenges": [{"id": "a", "key": "a", "category": "c", "icon": "STONE", "requires": "a",
        "preset": {"trigger": "t", "triggerSettings": {}, "action": "x", "actionSettings": {}}}]}
      """, "only apply to an entry with a class");
  }

  @Test
  void aModuleCannotReuseAnId(@TempDir Path dir) throws IOException {
    ChallengeCatalog module = load(dir, """
      {"package": "x", "challenges": [{"id": "cutclean", "key": "a", "category": "utility", "icon": "STONE", "class": "A"}]}
      """);
    assertThrows(IllegalArgumentException.class, () -> bundled.merge(module));
  }

  @Test
  void aModuleMayUseTheCategoriesOfThePlugin(@TempDir Path dir) throws IOException {
    ChallengeCatalog module = load(dir, """
      {"package": "x", "challenges": [{"id": "own", "key": "own", "category": "utility", "icon": "STONE", "class": "Own"}]}
      """);
    ChallengeCatalog merged = bundled.merge(module).requireComplete();
    assertEquals("own", merged.challengeOf("x.Own").orElseThrow().id());
  }

  @Test
  void completenessNamesEveryMissingReference(@TempDir Path dir) throws IOException {
    ChallengeCatalog module = load(dir, """
      {"challenges": [
        {"id": "a", "key": "a", "category": "nowhere", "icon": "STONE", "class": "A"},
        {"id": "b", "key": "b", "category": "utility", "icon": "STONE",
          "preset": {"trigger": "interval", "triggerSettings": {}, "action": "chunk_to_block", "actionSettings": {}}}
      ]}
      """);
    IllegalStateException error = assertThrows(IllegalStateException.class, () -> bundled.merge(module).requireComplete());
    assertTrue(error.getMessage().contains("a names the unknown category nowhere"), error.getMessage());
    assertTrue(error.getMessage().contains("b is built from a beta trigger or action"), error.getMessage());
  }

  private static void assertRejected(Path dir, String challenges, String message) throws IOException {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> load(dir, challenges));
    assertTrue(error.getMessage().contains(message), error.getMessage());
  }

  private static ChallengeCatalog load(Path dir, String challenges) throws IOException {
    Path file = dir.resolve(ChallengeCatalog.DIRECTORY + "challenges.json");
    Files.createDirectories(file.getParent());
    Files.writeString(file, challenges);
    try (URLClassLoader loader = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null)) {
      return ChallengeCatalog.load(loader);
    }
  }

}
