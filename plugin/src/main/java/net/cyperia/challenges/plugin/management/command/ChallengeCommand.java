package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * A command as a Brigadier tree. The tree is built for whatever source the server dispatches with,
 * Paper's own or one of a network's, and reaches the sender only through {@link CommandNodes}.
 */
public interface ChallengeCommand {

  @NotNull
  CommandInfo info();

  /**
   * Adds the arguments, subcommands and executors to {@code root}. The root already requires
   * {@link CommandInfo#getPermission()}.
   */
  <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes);

}
