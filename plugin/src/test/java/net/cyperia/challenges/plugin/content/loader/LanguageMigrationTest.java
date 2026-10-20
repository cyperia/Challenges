package net.cyperia.challenges.plugin.content.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageMigrationTest {

  private static JsonObject json(String json) {
    return JsonParser.parseString(json.replace('\'', '"')).getAsJsonObject();
  }

  @Test
  void updatesUncustomizedTextsWithoutABackup() {
    JsonObject extracted = json("{'a': 'old'}");
    LanguageMigration.Result result = LanguageMigration.migrate(extracted, json("{'a': 'new'}"), json("{'a': 'old'}"));
    assertEquals(json("{'a': 'new'}"), extracted);
    assertEquals(0, result.replacedCount());
  }

  @Test
  void keepsCustomizationsOfTextsThatDidNotChange() {
    JsonObject extracted = json("{'a': 'mine'}");
    LanguageMigration.Result result = LanguageMigration.migrate(extracted, json("{'a': 'same'}"), json("{'a': 'same'}"));
    assertEquals(json("{'a': 'mine'}"), extracted);
    assertEquals(0, result.replacedCount());
  }

  @Test
  void replacesCustomizationsOfChangedTextsIntoTheBackup() {
    JsonObject extracted = json("{'section': {'a': 'mine', 'b': 'kept'}}");
    LanguageMigration.Result result = LanguageMigration.migrate(extracted,
      json("{'section': {'a': 'new', 'b': 'same'}}"), json("{'section': {'a': 'old', 'b': 'same'}}"));
    assertEquals(json("{'section': {'a': 'new', 'b': 'kept'}}"), extracted);
    assertEquals(json("{'section': {'a': 'mine'}}"), result.replaced());
    assertEquals(1, result.replacedCount());
  }

  @Test
  void addsMissingTexts() {
    JsonObject extracted = json("{}");
    LanguageMigration.migrate(extracted, json("{'a': 'new', 'b': ['x', 'y']}"), json("{}"));
    assertEquals(json("{'a': 'new', 'b': ['x', 'y']}"), extracted);
  }

  @Test
  void withoutARecordReplacesEveryDifferingText() {
    JsonObject extracted = json("{'a': 'old', 'b': 'same'}");
    LanguageMigration.Result result = LanguageMigration.migrate(extracted, json("{'a': 'new', 'b': 'same'}"), null);
    assertEquals(json("{'a': 'new', 'b': 'same'}"), extracted);
    assertEquals(json("{'a': 'old'}"), result.replaced());
  }

  @Test
  void treatsADottedKeyAsOneKey() {
    JsonObject extracted = json("{'bar': {'up.*colorize*': 'mine'}}");
    LanguageMigration.migrate(extracted, json("{'bar': {'up.*colorize*': 'new'}}"), json("{'bar': {'up.*colorize*': 'old'}}"));
    assertEquals(json("{'bar': {'up.*colorize*': 'new'}}"), extracted);
  }

}
