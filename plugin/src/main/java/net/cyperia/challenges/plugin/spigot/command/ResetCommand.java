package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResetCommand implements ChallengeCommand {

  private final boolean confirmReset, seedResetCommand;

  public ResetCommand() {
    this(Challenges.getInstance().getConfigDocument());
  }

  private ResetCommand(@NotNull Document pluginConfig) {
    this(pluginConfig.getBoolean(ConfigManager.Keys.CONFIRM_RESET), pluginConfig.getBoolean(ConfigManager.Keys.CUSTOM_SEED_COMMAND));
  }

  ResetCommand(boolean confirmReset, boolean seedResetCommand) {
    this.confirmReset = confirmReset;
    this.seedResetCommand = seedResetCommand;
  }

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("reset", ChallengesPermissions.COMMAND_RESET);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.then(nodes.literal("settings").requires(nodes.permission(ChallengesPermissions.COMMAND_RESET_SETTINGS))
      .executes(nodes.run((sender, context) -> {
        Challenges.getInstance().getChallengeManager().restoreDefaults();
        MessageKey.of("command.reset.config").broadcast(Prefix.CHALLENGES);
      })));
    root.then(nodes.literal("customs").requires(nodes.permission(ChallengesPermissions.COMMAND_RESET_CUSTOMS))
      .executes(nodes.run((sender, context) -> {
        Challenges.getInstance().getCustomChallengesLoader().resetChallenges();
        MessageKey.of("command.reset.custom-challenges").broadcast(Prefix.CHALLENGES);
      })));

    root.then(worldReset(nodes.literal("confirm"), nodes));
    if (!confirmReset) {
      worldReset(root, nodes);
      return;
    }

    Command<S> confirmHint = nodes.run((sender, context) -> {
      if (rejectFresh(sender)) return;
      MessageKey.of("command.reset.confirm").send(sender, Prefix.CHALLENGES, "reset confirm");
    });
    root.executes(confirmHint);
    if (seedResetCommand) {
      root.then(nodes.argument("seed", StringArgumentType.greedyString()).executes(confirmHint));
    }
  }

  // The world reset itself, with a seed after it when custom-seed.command is on
  @NotNull
  private <S, T extends ArgumentBuilder<S, T>> T worldReset(@NotNull T node, @NotNull CommandNodes<S> nodes) {
    node.executes(nodes.run((sender, context) -> reset(sender, null)));
    if (seedResetCommand) {
      node.then(nodes.argument("seed", StringArgumentType.greedyString())
        .executes(nodes.run((sender, context) -> reset(sender, parseSeed(StringArgumentType.getString(context, "seed"))))));
    }
    return node;
  }

  private static void reset(@NotNull CommandSender sender, @Nullable Long seed) {
    if (rejectFresh(sender)) return;
    Challenges.getInstance().getWorldManager().prepareWorldReset(sender, seed);
  }

  private static boolean rejectFresh(@NotNull CommandSender sender) {
    if (Challenges.getInstance().getWorldManager().isEnableFreshReset() || !ChallengeAPI.isFresh()) return false;
    MessageKey.of("command.reset.not-fresh").send(sender, Prefix.CHALLENGES);
    SoundSample.BASS_OFF.playIfPlayer(sender);
    return true;
  }

  // Same rule as the vanilla world seed field: a number is the seed itself, any other text seeds by its hash
  static long parseSeed(@NotNull String input) {
    try {
      return Long.parseLong(input.trim());
    } catch (NumberFormatException exception) {
      return input.trim().hashCode();
    }
  }

}
