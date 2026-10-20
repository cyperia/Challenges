package net.cyperia.challenges.plugin.challenges.implementation;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Offers to play a beta entry of the public release on the server it is playable on.
 */
public final class BetaTransferDialog {

  public static final String HOST = "cyperia.net";
  private static final int PORT = 25565;
  private static final String DISCORD = "discord.cyperia.net";
  private static final int BUTTON_WIDTH = 150;
  private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
    .uses(1)
    .lifetime(Duration.ofMinutes(5))
    .build();

  private BetaTransferDialog() {
  }

  public static void show(@NotNull Player player, @NotNull ItemFormatter icon, @NotNull LocalizableMessage name,
                          @NotNull Consumer<Player> onCancel) {
    player.closeInventory();
    Locale locale = TranslationManager.getLocaleOf(player);
    player.showDialog(Dialog.create(factory -> factory.empty()
      .base(DialogBase.builder(MessageKey.of("menu.locked.title").asComponent(locale))
        .body(List.of(
          DialogBody.item(icon.build(locale))
            .description(DialogBody.plainMessage(name.asComponent(locale)))
            .showTooltip(false)
            .build(),
          DialogBody.plainMessage(MessageKey.of("menu.locked.body").withArgs(HOST).asComponent(locale)),
          DialogBody.plainMessage(MessageKey.of("menu.locked.request").withArgs(DISCORD).asComponent(locale))))
        .build())
      .type(DialogType.confirmation(
        ActionButton.builder(MessageKey.of("menu.locked.join").withArgs(HOST).asComponent(locale))
          .width(BUTTON_WIDTH)
          .action(DialogAction.customClick((view, audience) -> {
            if (audience instanceof Player target) target.transfer(HOST, PORT);
          }, CALLBACK_OPTIONS))
          .build(),
        ActionButton.builder(MessageKey.of("menu.locked.cancel").asComponent(locale))
          .width(BUTTON_WIDTH)
          .action(DialogAction.customClick((view, audience) -> {
            if (audience instanceof Player target) onCancel.accept(target);
          }, CALLBACK_OPTIONS))
          .build()))));
  }

}
