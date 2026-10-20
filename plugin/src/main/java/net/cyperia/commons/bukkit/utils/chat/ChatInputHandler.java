package net.cyperia.commons.bukkit.utils.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ChatInputHandler {

  final class Holder {

    private Holder() {
    }

    static final Map<Player, ChatInputHandler> handlers = new ConcurrentHashMap<>();

  }

  static void set(@NotNull Player player, @Nullable ChatInputHandler handler) {
    ChatInputHandler prev = Holder.handlers.put(player, handler);
    if (prev != null) {
      prev.handleCancel(player);
    }
  }

  static void cancel(@NotNull Player player) {
    ChatInputHandler prev = Holder.handlers.remove(player);
    if (prev != null) {
      prev.handleCancel(player);
    }
  }

  @Nullable
  static ChatInputHandler remove(@NotNull Player player) {
    return Holder.handlers.remove(player);
  }

  @Nullable
  static ChatInputHandler get(@NotNull Player player) {
    return Holder.handlers.get(player);
  }

  void handleChatInput(@NotNull AsyncChatEvent event, @NotNull String input);

  default void handleCancel(@NotNull Player player) {
  }

}
