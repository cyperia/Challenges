package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiteralCaseTest {

  private static final ChallengeCommand TIMER = new ChallengeCommand() {
    @NotNull
    @Override
    public CommandInfo info() {
      return CommandInfo.command("timer", "timer").withAliases("t");
    }

    @Override
    public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      root.then(nodes.literal("start"))
        .then(nodes.literal("set").then(nodes.argument("time", StringArgumentType.greedyString())));
    }
  };

  private static CommandNode<CommandSender> tree() {
    CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();
    CommandManager.registerLiterals(dispatcher, TIMER);
    return dispatcher.getRoot();
  }

  @Test
  void literalsTakeTheSpellingOfTheTree() {
    assertEquals("timer start", LiteralCase.normalize(tree(), "Timer START"));
    assertEquals("t start", LiteralCase.normalize(tree(), "T Start"));
  }

  @Test
  void argumentsKeepTheirCase() {
    assertEquals("timer set 1H 30Min", LiteralCase.normalize(tree(), "TIMER Set 1H 30Min"));
  }

  @Test
  void unknownInputIsLeftAlone() {
    assertEquals("Weather Clear", LiteralCase.normalize(tree(), "Weather Clear"));
    assertEquals("timer  start ", LiteralCase.normalize(tree(), "timer  START "));
  }

}
