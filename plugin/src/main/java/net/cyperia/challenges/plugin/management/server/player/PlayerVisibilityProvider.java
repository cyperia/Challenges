package net.cyperia.challenges.plugin.management.server.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PlayerVisibilityProvider {

  boolean isPlayerVanished(@NotNull Player player);

}
