package net.cyperia.challenges.plugin.spigot.command;

import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GamemodeCommandTest {

  @Test
  void getPermissionMapsEachGameModeToItsOwnNode() {
    assertEquals("challenges.gamemode.survival", GamemodeCommand.getPermission(GameMode.SURVIVAL));
    assertEquals("challenges.gamemode.creative", GamemodeCommand.getPermission(GameMode.CREATIVE));
    assertEquals("challenges.gamemode.adventure", GamemodeCommand.getPermission(GameMode.ADVENTURE));
    assertEquals("challenges.gamemode.spectator", GamemodeCommand.getPermission(GameMode.SPECTATOR));
  }

}
