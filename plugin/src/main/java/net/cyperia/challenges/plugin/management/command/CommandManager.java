package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CommandManager {

  private final List<ChallengeCommand> commands = new ArrayList<>();
  private CommandRegistrar registrar;
  private boolean registeredWithPaper;
  // Only set while the commands are registered with Paper; through a registrar its handler matches case
  private CommandNode<CommandSender> literalTree;

  /**
   * Paper builds its dispatcher once every plugin is enabled, so a registrar set by another plugin's
   * enable still replaces Paper's before anything is registered there.
   */
  public void enable(@NotNull Plugin plugin) {
    plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      if (registrar != null) return;
      CommandDispatcher<CommandSender> literals = new CommandDispatcher<>();
      for (ChallengeCommand command : commands) {
        try {
          registerWithPaper(event.registrar(), command);
          registerLiterals(literals, command);
        } catch (RuntimeException ex) {
          Logger.error("Could not register the command /{}", command.info().getLiteral(), ex);
        }
      }
      literalTree = literals.getRoot();
      registeredWithPaper = true;
    });
    plugin.getServer().getPluginManager().registerEvents(new CommandCaseListener(() -> registrar == null ? literalTree : null), plugin);
  }

  public void register(@NotNull ChallengeCommand command) {
    commands.add(command);
    if (registrar != null) {
      registrar.register(command);
    } else if (registeredWithPaper) {
      Logger.warn("The command /{} was registered after the server built its commands, it is available after the next reload", command.info().getLiteral());
    }
  }

  /**
   * Sends every command, those registered so far included, to {@code registrar} instead of Paper.
   */
  public void setRegistrar(@NotNull CommandRegistrar registrar) {
    if (registeredWithPaper) {
      Logger.warn("The commands were already registered with Paper, they stay there next to the new registrar");
    }
    this.registrar = registrar;
    commands.forEach(registrar::register);
  }

  private static void registerWithPaper(@NotNull Commands registrar, @NotNull ChallengeCommand command) {
    CommandInfo info = command.info();
    CommandNodes<CommandSourceStack> nodes = new CommandNodes<>(CommandManager::senderOf);
    var root = Commands.literal(info.getLiteral());
    nodes.define(command, root);
    String description = PlainTextComponentSerializer.plainText()
      .serialize(MessageKey.of(info.getDescriptionKey()).asComponent(TranslationManager.getServerLocale()));
    registrar.register(root.build(), description, info.getAliases());
  }

  // The same tree on Bukkit's sender, only read for the spelling of the literals
  static void registerLiterals(@NotNull CommandDispatcher<CommandSender> dispatcher, @NotNull ChallengeCommand command) {
    CommandNodes<CommandSender> nodes = new CommandNodes<>(sender -> sender);
    List<String> names = new ArrayList<>(List.of(command.info().getLiteral()));
    names.addAll(command.info().getAliases());
    for (String name : names) {
      var root = nodes.literal(name);
      nodes.define(command, root);
      dispatcher.register(root);
    }
  }

  // /execute as <player> run ... acts for the player, as the commands did when they were Bukkit commands
  @NotNull
  static CommandSender senderOf(@NotNull CommandSourceStack source) {
    return source.getExecutor() instanceof Player player ? player : source.getSender();
  }

}
