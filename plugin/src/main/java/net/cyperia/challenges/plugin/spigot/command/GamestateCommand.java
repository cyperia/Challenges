package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.common.config.FileDocument;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class GamestateCommand implements SenderCommand, Completer {

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {

    if (args.length != 1) {
      MessageKey.of("command.syntax").send(sender, Prefix.CHALLENGES, "gamestate <reset/reload>");
      return;
    }

    FileDocument gamestate = Challenges.getInstance().getConfigManager().getGamestateConfig();
    switch (args[0].toLowerCase()) {
      case "reset":
        gamestate.clear();
        Challenges.getInstance().getChallengeManager().resetGamestate();
        Challenges.getInstance().getScoreboardManager().updateAll();
        MessageKey.of("command.gamestate.reset").send(sender, Prefix.CHALLENGES);
        break;
      case "reload":
        Challenges.getInstance().getChallengeManager().loadGamestate(gamestate.readonly());
        Challenges.getInstance().getScoreboardManager().updateAll();
        MessageKey.of("command.gamestate.reloaded").send(sender, Prefix.CHALLENGES);
        break;
      default:
        MessageKey.of("command.syntax").send(sender, Prefix.CHALLENGES, "gamestate <reset/reload>");
    }

  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    return args.length == 1 ? Utils.filterRecommendations(args[0], "reset", "reload") : Collections.emptyList();
  }

}
