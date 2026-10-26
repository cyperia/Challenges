package net.cyperia.challenges.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CatalogExportTest {

  private static final Pattern LEFTOVER = Pattern.compile("<[^<>]*>|\\{\\d+}");

  @Test
  void committedExportIsCurrent() throws IOException {
    String committed = Files.readString(Path.of(CatalogExport.FILE)).replace("\r\n", "\n");
    assertEquals(CatalogExport.render(ChallengeCatalog.load()), committed,
      CatalogExport.FILE + " is out of date, run ./gradlew :catalog:exportCatalog and commit it");
  }

  @Test
  void textsKeepNoMarkupOrArguments() {
    String rendered = CatalogExport.render(ChallengeCatalog.load());
    assertFalse(LEFTOVER.matcher(rendered).find(), () -> "Leftover: " + LEFTOVER.matcher(rendered).results()
      .map(match -> match.group()).distinct().collect(Collectors.joining(", ")));
  }

  @Test
  void leavesOutUnregisteredAndHiddenChallenges() {
    ChallengeCatalog catalog = ChallengeCatalog.load();
    JsonObject export = JsonParser.parseString(CatalogExport.render(catalog)).getAsJsonObject();
    Set<String> exported = export.getAsJsonArray("challenges").asList().stream()
      .map(entry -> entry.getAsJsonObject().get("id").getAsString()).collect(Collectors.toSet());
    catalog.challenges().forEach(challenge -> assertEquals(challenge.registered() && !challenge.hidden(), exported.contains(challenge.id()), challenge.id()));
  }

  @Test
  void presetsNameExportedTriggersAndActions() {
    JsonObject export = JsonParser.parseString(CatalogExport.render(ChallengeCatalog.load())).getAsJsonObject();
    Set<String> triggers = ids(export, "triggers");
    Set<String> actions = ids(export, "actions");
    export.getAsJsonArray("challenges").asList().stream().map(entry -> entry.getAsJsonObject())
      .filter(entry -> entry.has("preset"))
      .forEach(entry -> {
        JsonObject preset = entry.getAsJsonObject("preset");
        assertTrue(triggers.contains(preset.get("trigger").getAsString()), entry.get("id").getAsString());
        assertTrue(actions.contains(preset.get("action").getAsString()), entry.get("id").getAsString());
      });
  }

  private static Set<String> ids(JsonObject export, String array) {
    return export.getAsJsonArray(array).asList().stream()
      .map(entry -> entry.getAsJsonObject().get("id").getAsString()).collect(Collectors.toSet());
  }

  @Test
  void runsKeepTheGameColours() {
    JsonArray runs = CatalogExport.runs("Each <gold>block</gold> drops <b><red>this</red></b>");
    assertEquals(4, runs.size());
    assertFalse(runs.get(0).getAsJsonObject().has("color"));
    assertEquals("#FFAA00", runs.get(1).getAsJsonObject().get("color").getAsString());
    assertEquals("this", runs.get(3).getAsJsonObject().get("text").getAsString());
    assertTrue(runs.get(3).getAsJsonObject().get("bold").getAsBoolean());
  }

  @Test
  void aGradientColoursEveryCharacter() {
    JsonArray runs = CatalogExport.runs("<gradient:#000000:#FFFFFF>abc</gradient>");
    assertEquals(3, runs.size());
    assertNotEquals(runs.get(0).getAsJsonObject().get("color"), runs.get(2).getAsJsonObject().get("color"));
  }

  @Test
  void plainDropsTags() {
    assertEquals("Hello World", CatalogExport.plain(" <gray>Hello <#9B8CFF>World</#9B8CFF></gray> "));
  }

}
