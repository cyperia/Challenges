package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandManagerTest {

  private static ChallengeCommand command(String literal) {
    return new ChallengeCommand() {
      @NotNull
      @Override
      public CommandInfo info() {
        return CommandInfo.command(literal, "test." + literal);
      }

      @Override
      public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      }
    };
  }

  @Test
  void aRegistrarGetsTheCommandsRegisteredBeforeAndAfterIt() {
    CommandManager manager = new CommandManager();
    ChallengeCommand before = command("before");
    ChallengeCommand after = command("after");
    List<ChallengeCommand> registered = new ArrayList<>();

    manager.register(before);
    manager.setRegistrar(registered::add);
    manager.register(after);

    assertEquals(List.of(before, after), registered);
  }

}
