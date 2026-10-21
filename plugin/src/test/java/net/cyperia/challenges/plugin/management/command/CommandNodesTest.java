package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.cyperia.challenges.plugin.management.command.TestSenders.senderWith;
import static org.junit.jupiter.api.Assertions.*;

class CommandNodesTest {

  private final CommandNodes<CommandSender> nodes = new CommandNodes<>(sender -> sender);

  private CommandDispatcher<CommandSender> dispatcher(ChallengeCommand command) {
    CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();
    LiteralArgumentBuilder<CommandSender> root = nodes.literal(command.info().getLiteral());
    nodes.define(command, root);
    dispatcher.register(root);
    return dispatcher;
  }

  private static final class ExampleCommand implements ChallengeCommand {

    int runs;

    @NotNull
    @Override
    public CommandInfo info() {
      return CommandInfo.command("example", "example.command");
    }

    @Override
    public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      root
        .executes(nodes.run((sender, context) -> runs++))
        .then(nodes.literal("sub").requires(nodes.permission("example.command.sub"))
          .executes(nodes.run((sender, context) -> runs++)))
        .then(nodes.literal("pick").then(nodes.argument("value", StringArgumentType.word())
          .suggests(nodes.suggest(sender -> List.of("Alpha", "beta", "Gamma")))));
    }

  }

  @Test
  void theRootNeedsTheCommandPermission() {
    CommandDispatcher<CommandSender> dispatcher = dispatcher(new ExampleCommand());
    assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("example", senderWith()));
  }

  @Test
  void aSubcommandNeedsItsOwnPermissionOnTop() throws CommandSyntaxException {
    ExampleCommand command = new ExampleCommand();
    CommandDispatcher<CommandSender> dispatcher = dispatcher(command);

    assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("example sub", senderWith("example.command")));
    dispatcher.execute("example sub", senderWith("example.command", "example.command.sub"));
    assertEquals(1, command.runs);
  }

  @Test
  void executorsReturnZeroBecauseNetworksReadOneAsAnError() throws CommandSyntaxException {
    assertEquals(0, dispatcher(new ExampleCommand()).execute("example", senderWith("example.command")));
  }

  @Test
  void suggestionsStartWithTheInputIgnoringCase() {
    CommandSender sender = senderWith("example.command");
    CommandDispatcher<CommandSender> dispatcher = dispatcher(new ExampleCommand());

    List<String> suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("example pick g", sender)).join()
      .getList().stream().map(Suggestion::getText).toList();

    assertEquals(List.of("Gamma"), suggestions);
  }

}
