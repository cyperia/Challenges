package net.cyperia.challenges.plugin.management.command;

import org.jetbrains.annotations.NotNull;

/**
 * Hands a command to the server. Without one set through {@link CommandManager#setRegistrar}, the
 * commands go to Paper's own dispatcher.
 */
public interface CommandRegistrar {

  void register(@NotNull ChallengeCommand command);

}
