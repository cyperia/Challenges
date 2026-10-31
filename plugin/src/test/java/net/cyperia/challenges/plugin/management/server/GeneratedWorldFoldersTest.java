package net.cyperia.challenges.plugin.management.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedWorldFoldersTest {

  @Test
  void generatedWorldsBesideTheLevelAreFound(@TempDir File container) {
    for (String name : new String[] {"world", "plugins", "void", "challenges-stronghold-world", "challenges-stronghold-world_nether", "challengesx"}) {
      assertTrue(new File(container, name).mkdirs());
    }

    Set<String> names = WorldManager.listGeneratedWorldFolders(container).stream().map(File::getName).collect(Collectors.toSet());

    assertEquals(Set.of("void", "challenges-stronghold-world", "challenges-stronghold-world_nether"), names);
  }

}
