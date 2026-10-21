package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.jetbrains.annotations.NotNull;

public class ChallengesCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("challenges", ChallengesPermissions.COMMAND_CHALLENGES).withAliases("c", "settings", "challenge");
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root
      .executes(nodes.runAsPlayer((player, context) -> Challenges.getInstance().getMenuManager().openMainMenu(player)))
      .then(nodes.argument("menu", StringArgumentType.word())
        .suggests(nodes.suggest(sender -> MenuType.values().stream().map(MenuType::getKey).toList()))
        .executes(nodes.runAsPlayer((player, context) -> {
          MenuType<?> menuType = MenuType.find(StringArgumentType.getString(context, "menu"));
          if (menuType == null) {
            Challenges.getInstance().getMenuManager().openMainMenu(player);
          } else {
            Challenges.getInstance().getMenuManager().openMenu(player, menuType, 0);
            SoundSample.OPEN.play(player);
          }
        })));
  }

}
