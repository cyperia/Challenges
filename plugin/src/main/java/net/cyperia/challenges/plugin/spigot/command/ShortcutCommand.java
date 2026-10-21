package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.command.CommandNodes.PlayerAction;
import net.cyperia.challenges.plugin.management.command.CommandNodes.SenderAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ShortcutCommand implements ChallengeCommand {

  private final CommandInfo info;
  private final SenderAction senderAction;
  private final PlayerAction playerAction;

  private ShortcutCommand(@NotNull CommandInfo info, @Nullable SenderAction senderAction, @Nullable PlayerAction playerAction) {
    this.info = info;
    this.senderAction = senderAction;
    this.playerAction = playerAction;
  }

  @NotNull
  public static ShortcutCommand forSender(@NotNull CommandInfo info, @NotNull SenderAction action) {
    return new ShortcutCommand(info, action, null);
  }

  @NotNull
  public static ShortcutCommand forPlayer(@NotNull CommandInfo info, @NotNull PlayerAction action) {
    return new ShortcutCommand(info, null, action);
  }

  @NotNull
  @Override
  public CommandInfo info() {
    return info;
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.executes(senderAction != null ? nodes.run(senderAction) : nodes.runAsPlayer(playerAction));
  }

}
