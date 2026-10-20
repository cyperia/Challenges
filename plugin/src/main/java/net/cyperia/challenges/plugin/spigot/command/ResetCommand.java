package net.cyperia.challenges.plugin.spigot.command;

import com.google.common.collect.Lists;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ResetCommand implements SenderCommand, Completer {

  private final boolean confirmReset, seedResetCommand;

  public ResetCommand() {
    Document pluginConfig = Challenges.getInstance().getConfigDocument();
    confirmReset = pluginConfig.getBoolean(ConfigManager.Keys.CONFIRM_RESET);
    seedResetCommand = pluginConfig.getBoolean(ConfigManager.Keys.CUSTOM_SEED_COMMAND);
  }

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

    if (confirmReset && (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) || (args.length > 0 && !args[0].equalsIgnoreCase("confirm"))) {
      if (args.length > 0 && args[0].equalsIgnoreCase("settings")) {
        Challenges.getInstance().getChallengeManager().restoreDefaults();
        MessageKey.of("command.reset.config").broadcast(Prefix.CHALLENGES);
        return;
      } else if (args.length > 0 && args[0].equalsIgnoreCase("customs")) {
        Challenges.getInstance().getCustomChallengesLoader().resetChallenges();
        MessageKey.of("command.reset.custom-challenges").broadcast(Prefix.CHALLENGES);
        return;
      }

      if (!Challenges.getInstance().getWorldManager().isEnableFreshReset() && ChallengeAPI.isFresh()) {
        MessageKey.of("command.reset.not-fresh").send(sender, Prefix.CHALLENGES);
        SoundSample.BASS_OFF.playIfPlayer(sender);
        return;
      }

      if (confirmReset) {
        MessageKey.of("command.reset.confirm").send(sender, Prefix.CHALLENGES, "reset confirm");
        return;
      }
    }

    if (!Challenges.getInstance().getWorldManager().isEnableFreshReset() && ChallengeAPI.isFresh()) {
      MessageKey.of("command.reset.not-fresh").send(sender, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }

    Long seed = null;

    if (seedResetCommand) {
      int index = confirmReset ? 1 : 0;
      if (args.length > index) {
        String seedInput = args[index];
        try {
          seed = Long.parseLong(seedInput);
        } catch (NumberFormatException exception) {
          Challenges.getInstance().getILogger().error("", exception);
        }
      }
    }

    Challenges.getInstance().getWorldManager().prepareWorldReset(sender, seed);

  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (confirmReset && args.length == 1) return Utils.filterRecommendations(
      args[0], "confirm", "settings", "customs");
    if (seedResetCommand && ((confirmReset && args.length == 2) || args.length == 1)) {
      return args.length == 1 ?
        Utils.filterRecommendations(args[args.length - 1], "[seed]", "settings", "customs") :
        args[0].equalsIgnoreCase("confirm") ? Collections.singletonList("[seed]") : Lists
          .newLinkedList();
    }

    return Lists.newLinkedList();
  }

}
