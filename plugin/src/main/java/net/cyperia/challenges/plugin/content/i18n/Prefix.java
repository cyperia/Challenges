package net.cyperia.challenges.plugin.content.i18n;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Prefix implements KeyedLocalizableMessage {

  // The prefix.challenges look from global.json, for messages that do not come from the language files
  public static final Component FALLBACK_COMPONENT = MiniMessage.miniMessage().deserialize(
    "<dark_gray><b>┃</b></dark_gray> <gradient:#5B3FE4:#8A3FCF>Challenges</gradient> <dark_gray>┃</dark_gray> ");
  public static final String FALLBACK = LegacyComponentSerializer.builder().character('§').hexColors()
    .useUnusualXRepeatedCharacterHexFormat().build().serialize(FALLBACK_COMPONENT);

  public static final Prefix
    CHALLENGES = create("challenges"),
    CUSTOM = create("custom"),
    DAMAGE = create("damage"),
    POSITION = create("position"),
    BACKPACK = create("backpack"),
    TIMER = create("timer"),
    QUIZ = create("quiz");

  private final String messageKey;

  @NotNull
  @Override
  public MessageKey getLocalizableKey() {
    return MessageKey.of(messageKey);
  }

  @NotNull
  @Contract(pure = true)
  public static Prefix create(@NotNull String messageKeySuffix) {
    return new Prefix("prefix." + messageKeySuffix);
  }

}
