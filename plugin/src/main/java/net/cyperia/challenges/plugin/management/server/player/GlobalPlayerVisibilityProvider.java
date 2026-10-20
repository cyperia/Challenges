package net.cyperia.challenges.plugin.management.server.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GlobalPlayerVisibilityProvider implements PlayerVisibilityProvider {

  @Override
  public boolean isPlayerVanished(@NotNull Player player) {
    return false;
  }
}
