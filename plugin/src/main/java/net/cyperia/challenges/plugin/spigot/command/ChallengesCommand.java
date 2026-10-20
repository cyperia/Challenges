package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChallengesCommand implements PlayerCommand, Completer {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) {
    if (args.length > 1) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "challenges [menu]");
      return;
    }

    if (args.length == 1) {
      MenuType<?> menuType = MenuType.find(args[0]);
      if (menuType == null) {
        Challenges.getInstance().getMenuManager().openMainMenu(player);
      } else {
        Challenges.getInstance().getMenuManager().openMenu(player, menuType, 0);
        SoundSample.OPEN.play(player);
      }
      return;
    }

    Challenges.getInstance().getMenuManager().openMainMenu(player);
  }


  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    return args.length != 1 ? null : Utils.filterRecommendations(args[0],
      MenuType.values().stream()
        .map(MenuType::getKey)
        .toArray(String[]::new)
    );
  }

}
