package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.utils.misc.CommandHelper;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * {@code /<command> [player]}: on yourself, or with the {@code .other} node on the players a name or
 * selector ({@code @a @r @p @s}) stands for.
 */
public abstract class PlayerTargetCommand implements ChallengeCommand {

  private final CommandInfo info;
  private final String otherPermission;
  private final String doneOthersKey;

  protected PlayerTargetCommand(@NotNull CommandInfo info, @NotNull String otherPermission, @NotNull String doneOthersKey) {
    this.info = info;
    this.otherPermission = otherPermission;
    this.doneOthersKey = doneOthersKey;
  }

  @NotNull
  @Override
  public CommandInfo info() {
    return info;
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    withTargets(root, nodes, otherPermission, (sender, targets) -> {
      if (changeAll(sender, targets, this::applyTo)) {
        MessageKey.of(doneOthersKey).send(sender, Prefix.CHALLENGES, targets.size());
      }
    });
  }

  protected abstract void applyTo(@NotNull Player target);

  @NotNull
  static <S, T extends ArgumentBuilder<S, T>> T withTargets(@NotNull T node, @NotNull CommandNodes<S> nodes, @NotNull String otherPermission, @NotNull TargetAction action) {
    return node
      .executes(nodes.run((sender, context) -> action.run(sender, sender instanceof Player player ? List.of(player) : List.of())))
      .then(nodes.argument("player", StringArgumentType.greedyString())
        .requires(nodes.permission(otherPermission))
        .suggests(nodes.suggest(CommandHelper::getCompletions))
        .executes(nodes.run((sender, context) ->
          action.run(sender, CommandHelper.getPlayers(sender, StringArgumentType.getString(context, "player").trim())))));
  }

  // Each change runs on its target's thread. True when one of the targets is somebody other than the sender
  static boolean changeAll(@NotNull CommandSender sender, @NotNull List<Player> targets, @NotNull Consumer<Player> change) {
    if (targets.isEmpty()) {
      MessageKey.of("command.no-target").send(sender, Prefix.CHALLENGES);
      return false;
    }

    boolean otherPlayers = false;
    for (Player target : targets) {
      Regions.entity(Challenges.getInstance(), target, () -> change.accept(target));
      if (target != sender) otherPlayers = true;
    }
    return otherPlayers;
  }

  @FunctionalInterface
  interface TargetAction {
    void run(@NotNull CommandSender sender, @NotNull List<Player> targets);
  }

}
