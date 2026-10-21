package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SearchCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("search", ChallengesPermissions.COMMAND_SEARCH);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root
      .executes(nodes.runAsPlayer((player, context) -> search(player, "")))
      .then(nodes.argument("query", StringArgumentType.greedyString())
        .executes(nodes.runAsPlayer((player, context) -> search(player, StringArgumentType.getString(context, "query")))));
  }

  // The dialog handles a query that is too short or matches nothing itself, so there is nothing to validate here
  private static void search(@NotNull Player player, @NotNull String query) {
    if (query.isBlank()) {
      MessageKey.of("command.search.open").send(player, Prefix.CHALLENGES);
    } else {
      MessageKey.of("command.search.query").send(player, Prefix.CHALLENGES, Component.text(query));
    }
    SoundSample.PLOP.play(player);
    MenuType.SEARCH.getMenuGenerator().openMenu(player, query);
  }

}
