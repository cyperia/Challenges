package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.chat.ChatInputHandler;
import org.jetbrains.annotations.NotNull;

public class CancelChatInputCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("cancelinput", ChallengesPermissions.COMMAND_CANCELINPUT);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.executes(nodes.runAsPlayer((player, context) -> {
      ChatInputHandler handler = ChatInputHandler.get(player);
      if (handler == null) {
        MessageKey.of("command.cancel-input.none").send(player, Prefix.CHALLENGES);
        return;
      }

      SoundSample.BASS_OFF.play(player);
      MessageKey.of("command.cancel-input.done").send(player, Prefix.CHALLENGES);
      ChatInputHandler.cancel(player);
    }));
  }

}
