package net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import net.cyperia.challenges.plugin.utils.misc.MapUtils;
import net.cyperia.commons.bukkit.utils.chat.ChatInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class TextInputSubSettingsBuilder extends SubSettingsBuilder {

  private final Consumer<Player> onOpen;
  private final BiPredicate<Player, String> isValid;

  public TextInputSubSettingsBuilder(String key) {
    this(key, null);
  }

  public TextInputSubSettingsBuilder(String key, SubSettingsBuilder parent) {
    this(key, parent, _ -> {
    }, (_, _) -> true);
  }

  public TextInputSubSettingsBuilder(String key,
                                     Consumer<Player> onOpen,
                                     BiPredicate<Player, String> isValid) {
    super(key);
    this.onOpen = onOpen;
    this.isValid = isValid;
  }

  public TextInputSubSettingsBuilder(String key,
                                     SubSettingsBuilder parent,
                                     Consumer<Player> onOpen,
                                     BiPredicate<Player, String> isValid) {
    super(key, parent);
    this.onOpen = onOpen;
    this.isValid = isValid;
  }

  @Override
  public boolean open(Player player, IParentCustomGenerator parentGenerator, LocalizableMessage title) {
    player.closeInventory();
    onOpen.accept(player);
    ChatInputHandler.set(player, new TextInputHandler(parentGenerator));
    return true;
  }

  @NotNull
  @Override
  public Collection<SubSettingDisplay> getCurrentDisplayFor(@NotNull Map<String, String[]> activated) {
    // TODO ported logic from legacy code; needs optimization / complete impl overhaul
    String[] values = activated.get(this.getKey());
    if (values == null) return Collections.emptyList();

    List<SubSettingDisplay> display = new ArrayList<>(values.length);
    for (String value : values) {
      display.add(new SubSettingDisplay(getKeyTranslation(this.getKey()), LocalizableMessage.wrap(value)));
    }

    return display;
  }

  @Override
  public boolean hasSettings() {
    return true;
  }

  @RequiredArgsConstructor
  public class TextInputHandler implements ChatInputHandler {

    private final IParentCustomGenerator parentGenerator;

    @Override
    public void handleChatInput(@NotNull AsyncChatEvent event, @NotNull String input) {
      if (!isValid.test(event.getPlayer(), input)) {
        Bukkit.getScheduler().runTask(Challenges.getInstance(), () -> {
          parentGenerator.decline(event.getPlayer());
        });
        return;
      }
      Bukkit.getScheduler().runTask(Challenges.getInstance(), () -> {
        parentGenerator.accept(event.getPlayer(), null, MapUtils.createStringArrayMap(getKey(), input));
      });
    }

    @Override
    public void handleCancel(@NotNull Player player) {
      parentGenerator.decline(player);
    }
  }

}
