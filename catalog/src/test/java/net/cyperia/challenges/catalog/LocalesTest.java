package net.cyperia.challenges.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class LocalesTest {

  @ParameterizedTest
  @ValueSource(strings = {"en", "de"})
  void everyCatalogEntryHasAResolvedName(String language) {
    Map<String, String[]> messages = Locales.load(language);
    ChallengeCatalog catalog = ChallengeCatalog.load();
    catalog.challenges().forEach(challenge -> assertResolved(messages, challenge.nameKey()));
    catalog.categories().forEach(category -> assertResolved(messages, category.nameKey()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"en", "de"})
  void aCatalogNameExistsInEveryLanguageOrNone(String language) {
    Map<String, String[]> messages = Locales.load(language);
    Map<String, String[]> english = Locales.load("en");
    ChallengeCatalog.load().challenges().stream()
      .filter(challenge -> english.containsKey(challenge.catalogNameKey()))
      .forEach(challenge -> assertResolved(messages, challenge.catalogNameKey()));
  }

  @Test
  void resolvesReferencesAndColorizersAndDropsMetaTags() {
    Map<String, String[]> resolved = Locales.resolve(Map.of(
      "word", new String[]{"World"},
      "challenge.x.*colorize*", new String[]{"<red>{0}</red>"},
      "challenge.x.name", new String[]{"Hello *{@word}*"}
    ));
    assertArrayEquals(new String[]{"Hello <red>World</red>"}, resolved.get("challenge.x.name"));
    assertFalse(resolved.containsKey("challenge.x.*colorize*"));
  }

  @Test
  void rejectsALanguageThatIsNotBundled() {
    assertThrows(IllegalArgumentException.class, () -> Locales.load("xx"));
  }

  @Test
  void aReferenceWithArgumentsResolvesInsideAHover() {
    String link = String.join("", Locales.load("en").get("challenge.generic.goal-link"));
    assertTrue(link.contains("<hover:show_text:'<gray>Left-Click</gray> <dark_gray>┃</dark_gray> <gray>Show the whole list</gray>'>"), link);
  }

  @ParameterizedTest
  @ValueSource(strings = {"en", "de"})
  void everyHoverIsAClickHint(String language) {
    Map<String, String[]> messages = Locales.load(language);
    String clickHint = "<hover:show_text:'" + String.join("", messages.get("menu.generic.tooltip.left")).split("\\{0}")[0];
    messages.forEach((key, value) -> {
      String text = String.join("\n", value);
      int hovers = text.split("<hover:show_text:'", -1).length - 1;
      assertEquals(hovers, text.split(Pattern.quote(clickHint), -1).length - 1, key + ": " + text);
    });
  }

  @Test
  void aPositionalArgumentInsideAHoverIsKeptForTheCallSite() {
    String option = String.join("", Locales.load("en").get("challenge.quiz.question.option"));
    assertTrue(option.contains("Answer with <yellow>/guess {0}</yellow>"), option);
  }

  private static void assertResolved(Map<String, String[]> messages, String key) {
    String[] value = messages.get(key);
    assertNotNull(value, key);
    String text = String.join("\n", value);
    assertFalse(text.isBlank(), key);
    assertFalse(text.contains("{@"), key + ": " + text);
    assertFalse(text.contains("*"), key + ": " + text);
  }

}
