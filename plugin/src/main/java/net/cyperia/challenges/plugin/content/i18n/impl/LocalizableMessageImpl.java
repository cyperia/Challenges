package net.cyperia.challenges.plugin.content.i18n.impl;

import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.content.i18n.KeyedLocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class LocalizableMessageImpl implements KeyedLocalizableMessage {

  private final AbstractMessageKey messageKey;
  private final Object[] args;

  @NotNull
  @Override
  public Component asComponent(@NotNull Locale locale) {
    return messageKey.asComponent(locale, args);
  }

  @NotNull
  @Override
  public List<Component> asComponents(@NotNull Locale locale) {
    return messageKey.asComponents(locale, args);
  }

  @NotNull
  @Override
  public KeyedLocalizableMessage withArgs(@NotNull Object... args) {
    return new LocalizableMessageImpl(messageKey, args);
  }

  @NotNull
  @Override
  public MessageKey getLocalizableKey() {
    return messageKey;
  }

  @NotNull
  @Override
  public Object[] getLocalizableArgs() {
    return args;
  }
}
