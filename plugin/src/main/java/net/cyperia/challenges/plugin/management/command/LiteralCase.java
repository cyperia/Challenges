package net.cyperia.challenges.plugin.management.command;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

/**
 * Brigadier matches literals by exact string, so {@code /Timer START} would be unknown. This turns
 * the leading literals of an input into the spelling of the tree and stops at the first token that
 * is not one, so argument values like player names keep their case.
 */
final class LiteralCase {

  private LiteralCase() {
  }

  @NotNull
  static String normalize(@NotNull CommandNode<?> root, @NotNull String input) {
    CommandNode<?> node = root;
    StringBuilder result = new StringBuilder(input.length());
    int cursor = 0;
    while (cursor < input.length()) {
      while (cursor < input.length() && input.charAt(cursor) == ' ') {
        result.append(' ');
        cursor++;
      }
      int start = cursor;
      while (cursor < input.length() && input.charAt(cursor) != ' ') {
        cursor++;
      }
      if (start == cursor) break;

      CommandNode<?> match = literal(node, input.substring(start, cursor));
      if (match == null) {
        return result.append(input, start, input.length()).toString();
      }
      result.append(match.getName());
      node = match;
    }
    return result.toString();
  }

  private static CommandNode<?> literal(@NotNull CommandNode<?> node, @NotNull String token) {
    for (CommandNode<?> child : node.getChildren()) {
      if (child instanceof LiteralCommandNode && child.getName().equalsIgnoreCase(token)) return child;
    }
    return null;
  }

}
