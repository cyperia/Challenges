package net.cyperia.challenges.plugin.permission;

import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChallengesPermissionsTest {

  @Test
  void eachGameModeHasItsOwnNodeUnderTheCommand() {
    assertEquals("challenges.command.gamemode.survival", ChallengesPermissions.commandGamemode(GameMode.SURVIVAL));
    assertEquals("challenges.command.gamemode.creative", ChallengesPermissions.commandGamemode(GameMode.CREATIVE));
    assertEquals("challenges.command.gamemode.adventure", ChallengesPermissions.commandGamemode(GameMode.ADVENTURE));
    assertEquals("challenges.command.gamemode.spectator", ChallengesPermissions.commandGamemode(GameMode.SPECTATOR));
  }

  @Test
  void everyCommandNodeIsUnderChallengesCommand() throws IllegalAccessException {
    for (Field field : ChallengesPermissions.class.getFields()) {
      if (!Modifier.isStatic(field.getModifiers()) || !field.getName().startsWith("COMMAND_")) continue;
      String node = (String) field.get(null);
      assertTrue(node.startsWith("challenges.command."), field.getName() + " is " + node);
    }
  }

}
