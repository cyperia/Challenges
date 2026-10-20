package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting.Pairing;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomizerCommandTest {

  private static final Pairing STONE_DROPS_LIKE_DIAMOND_ORE = new Pairing(Material.STONE, List.of(Material.DIAMOND_ORE));

  @Test
  void matchesBothSidesOfAPairing() {
    assertTrue(RandomizerCommand.matches(STONE_DROPS_LIKE_DIAMOND_ORE, "stone"));
    assertTrue(RandomizerCommand.matches(STONE_DROPS_LIKE_DIAMOND_ORE, "diamond"));
    assertTrue(RandomizerCommand.matches(STONE_DROPS_LIKE_DIAMOND_ORE, "diamond_ore"));
  }

  @Test
  void ignoresNamesOutsideThePairing() {
    assertFalse(RandomizerCommand.matches(STONE_DROPS_LIKE_DIAMOND_ORE, "dirt"));
  }

}
