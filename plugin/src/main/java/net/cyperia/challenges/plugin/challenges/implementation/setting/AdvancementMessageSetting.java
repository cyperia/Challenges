package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentAdvancementNames;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentPlayerNames;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentRewriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;

public class AdvancementMessageSetting extends Setting {

  public AdvancementMessageSetting() {
    super(true);
  }

  @EventHandler
  public void onAdvancement(@NotNull PlayerAdvancementDoneEvent event) {
    if (!isEnabled()) {
      event.message(null);
      return;
    }

    Component original = event.message();
    if (original == null) return;

    // the message is client side translated, so the same component goes out to everyone
    Component message = ComponentRewriter.rewrite(original, ComponentPlayerNames.REWRITE, ComponentAdvancementNames.REWRITE)
      .colorIfAbsent(NamedTextColor.GRAY);
    broadcastAudience(target -> target.sendMessage(Prefix.CHALLENGES.asComponent(target).append(message)));
    event.message(null);
  }

}
