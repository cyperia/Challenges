package net.cyperia.commons.bukkit.utils.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public final class ChatInputListener implements Listener {

  public ChatInputListener() {
    ChatInputHandler.Holder.handlers.clear();
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onChat(@NotNull AsyncChatEvent event) {
    ChatInputHandler handler = ChatInputHandler.remove(event.getPlayer());
    if (handler == null) return;

    Component messageComponent = event.message();
    String rawPlayerInput = PlainTextComponentSerializer.plainText().serialize(messageComponent);
    handler.handleChatInput(event, rawPlayerInput);

    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onQuit(@NotNull PlayerQuitEvent event) {
    ChatInputHandler.cancel(event.getPlayer());
  }

}
