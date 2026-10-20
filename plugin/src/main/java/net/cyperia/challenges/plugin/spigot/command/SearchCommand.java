package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SearchCommand implements PlayerCommand, Completer {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) {
    // no arguments simply opens the empty search menu; the dialog handles a query that is too short
    // or matches nothing itself, so there is nothing to validate here
    String query = String.join(" ", args);

    if (query.isBlank()) {
      MessageKey.of("command.search.open").send(player, Prefix.CHALLENGES);
    } else {
      MessageKey.of("command.search.query").send(player, Prefix.CHALLENGES, Component.text(query));
    }

    SoundSample.PLOP.play(player);
    MenuType.SEARCH.getMenuGenerator().openMenu(player, query);
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    // free text query; without this bukkit would suggest online player names
    return List.of();
  }

}
