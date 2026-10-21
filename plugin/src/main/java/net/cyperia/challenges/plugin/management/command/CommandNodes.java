package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builds the nodes of a {@link ChallengeCommand} for one source type, the only place that knows how
 * to get the {@link CommandSender} out of it.
 */
public final class CommandNodes<S> {

  private final Function<S, CommandSender> senders;

  public CommandNodes(@NotNull Function<S, CommandSender> senders) {
    this.senders = senders;
  }

  public void define(@NotNull ChallengeCommand command, @NotNull LiteralArgumentBuilder<S> root) {
    root.requires(permission(command.info().getPermission()));
    command.define(root, this);
  }

  @NotNull
  public CommandSender sender(@NotNull S source) {
    return senders.apply(source);
  }

  @NotNull
  public LiteralArgumentBuilder<S> literal(@NotNull String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  @NotNull
  public <T> RequiredArgumentBuilder<S, T> argument(@NotNull String name, @NotNull ArgumentType<T> type) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  @NotNull
  public Predicate<S> permission(@NotNull String node) {
    return source -> sender(source).hasPermission(node);
  }

  @NotNull
  public Command<S> run(@NotNull SenderAction action) {
    return context -> {
      CommandSender sender = sender(context.getSource());
      try {
        action.run(sender, context);
      } catch (CommandSyntaxException ex) {
        throw ex;
      } catch (Exception ex) {
        sender.sendMessage(Prefix.FALLBACK + "§cSomething went wrong while executing the command");
        Logger.error("Something went wrong while processing the command '{}'", context.getInput(), ex);
      }
      // A network's command handler reads 1 as an error code
      return 0;
    };
  }

  @NotNull
  public Command<S> runAsPlayer(@NotNull PlayerAction action) {
    return run((sender, context) -> {
      if (!(sender instanceof Player player)) {
        MessageKey.of("command.player-only").send(sender, Prefix.CHALLENGES);
        return;
      }
      action.run(player, context);
    });
  }

  /**
   * Suggests the values starting with what was typed so far, ignoring case.
   */
  @NotNull
  public SuggestionProvider<S> suggest(@NotNull Function<CommandSender, ? extends Iterable<String>> values) {
    return (context, builder) -> {
      String typed = builder.getRemainingLowerCase();
      for (String value : values.apply(sender(context.getSource()))) {
        if (value.toLowerCase(Locale.ROOT).startsWith(typed)) {
          builder.suggest(value);
        }
      }
      return builder.buildFuture();
    };
  }

  @FunctionalInterface
  public interface SenderAction {
    void run(@NotNull CommandSender sender, @NotNull CommandContext<?> context) throws Exception;
  }

  @FunctionalInterface
  public interface PlayerAction {
    void run(@NotNull Player player, @NotNull CommandContext<?> context) throws Exception;
  }

}
