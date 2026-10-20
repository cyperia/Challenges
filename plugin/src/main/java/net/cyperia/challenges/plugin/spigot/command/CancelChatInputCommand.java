package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.chat.ChatInputHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CancelChatInputCommand implements PlayerCommand {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
    ChatInputHandler handler = ChatInputHandler.get(player);
    if (handler == null) {
      MessageKey.of("command.cancel-input.none").send(player, Prefix.CHALLENGES);
      return;
    }

    SoundSample.BASS_OFF.play(player);
    MessageKey.of("command.cancel-input.done").send(player, Prefix.CHALLENGES);
    ChatInputHandler.cancel(player);
  }

}
