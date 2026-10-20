package net.cyperia.challenges.plugin.content.i18n;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public interface KeyedLocalizableMessage extends LocalizableMessage {

  @NotNull
  @Override
  MessageKey getLocalizableKey();

  @NotNull
  @Override
  default Component asComponent(@NotNull Locale locale) {
    return getLocalizableKey().asComponent(locale);
  }

  @NotNull
  @Override
  default List<Component> asComponents(@NotNull Locale locale) {
    return getLocalizableKey().asComponents(locale);
  }

  @NotNull
  @Override
  @Contract(pure = true)
  default KeyedLocalizableMessage withArgs(@NotNull Object... args) {
    return getLocalizableKey().withArgs(args);
  }

  @NotNull
  @Override
  default Object[] getLocalizableArgs() {
    return EMPTY_ARGS;
  }
}
