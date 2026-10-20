package net.cyperia.challenges.plugin.management.cloud;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public interface CloudSupport {

  void setIngame();

  void setLobby();

  void startNewService();

  /**
   * The challenge is over and the server is about to stop or restart. A cloud that tells a finished
   * server apart from a crashed one hears it here.
   */
  default void setRoundOver() {
  }

  /**
   * A reset is about to kick everyone and stop the server. A cloud that moves the players onto a
   * fresh server instead does it here and returns {@code true}, and the reset ends without touching
   * this server's worlds or configs.
   */
  default boolean handOverReset(@Nullable CommandSender requestedBy) {
    return false;
  }

}
