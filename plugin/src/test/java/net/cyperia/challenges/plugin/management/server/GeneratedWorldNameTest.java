package net.cyperia.challenges.plugin.management.server;

import org.bukkit.World.Environment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratedWorldNameTest {

  @Test
  void dimensionsFollowTheVanillaSuffixes() {
    assertEquals("challenges-voidworld", GameWorldStorage.getGeneratedWorldName("challenges-voidworld", Environment.NORMAL));
    assertEquals("challenges-voidworld_nether", GameWorldStorage.getGeneratedWorldName("challenges-voidworld", Environment.NETHER));
    assertEquals("challenges-voidworld_the_end", GameWorldStorage.getGeneratedWorldName("challenges-voidworld", Environment.THE_END));
  }

}
