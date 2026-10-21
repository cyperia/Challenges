package net.cyperia.challenges.plugin.management.command;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandProvider {

  @NotNull
  List<ChallengeCommand> getCommands();

}
