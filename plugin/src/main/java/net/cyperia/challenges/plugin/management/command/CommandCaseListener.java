package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

final class CommandCaseListener implements Listener {

  private static final String NAMESPACE = "challenges:";

  private final Supplier<CommandNode<CommandSender>> tree;

  CommandCaseListener(@NotNull Supplier<CommandNode<CommandSender>> tree) {
    this.tree = tree;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerCommand(@NotNull PlayerCommandPreprocessEvent event) {
    String normalized = normalize(event.getMessage().substring(1));
    if (normalized != null) event.setMessage("/" + normalized);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onServerCommand(@NotNull ServerCommandEvent event) {
    String normalized = normalize(event.getCommand());
    if (normalized != null) event.setCommand(normalized);
  }

  @Nullable
  private String normalize(@NotNull String command) {
    CommandNode<CommandSender> root = tree.get();
    if (root == null) return null;
    boolean namespaced = command.regionMatches(true, 0, NAMESPACE, 0, NAMESPACE.length());
    String body = namespaced ? command.substring(NAMESPACE.length()) : command;
    String normalized = LiteralCase.normalize(root, body);
    if (normalized.equals(body)) return null;
    return namespaced ? NAMESPACE + normalized : normalized;
  }

}
