package net.cyperia.commons.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentSaveTest {

  @TempDir
  Path directory;

  @Test
  void savingReplacesTheFileAndLeavesNothingElse() throws IOException {
    Path file = directory.resolve("data").resolve("gamestate.json");

    Document.create().set("round", 1).saveToFile(file);
    Document.create().set("round", 2).saveToFile(file.toFile());

    assertEquals(2, Document.parseJson(Files.readString(file)).getInt("round"));
    try (var files = Files.list(file.getParent())) {
      assertEquals(List.of(file), files.toList());
    }
  }

}
