package net.cyperia.challenges.plugin.management.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * What a {@link ChallengeCommand} is called and who may run it. The description is the language key
 * {@link #getDescriptionKey()}.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandInfo {

  private final String literal;
  private final String permission;
  private final List<String> aliases;
  /**
   * Knows nothing of the challenge running (heal, gamemode, time, ...). A server that ships its own
   * toolbox can leave these out and keep one version of each.
   */
  private final boolean generic;

  @NotNull
  public static CommandInfo command(@NotNull String literal, @NotNull String permission) {
    return new CommandInfo(literal, permission, List.of(), false);
  }

  @NotNull
  public CommandInfo withAliases(@NotNull String... aliases) {
    return new CommandInfo(literal, permission, List.of(aliases), generic);
  }

  @NotNull
  public CommandInfo asGeneric() {
    return new CommandInfo(literal, permission, aliases, true);
  }

  @NotNull
  public String getDescriptionKey() {
    return "command." + literal + ".description";
  }

}
