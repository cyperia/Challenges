package net.cyperia.challenges.plugin.content.i18n.impl.dynamic;

import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.AbstractMessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@RequiredArgsConstructor
public class DynamicMessageImpl implements LocalizableMessage {

  private final Function<Locale, String[]> valueFunction;
  private final Object[] args;

  @NotNull
  @Override
  public Component asComponent(@NotNull Locale locale) {
    return ComponentFormatter.deserializeLinesWithArgs(null, valueFunction.apply(locale),
      AbstractMessageKey.localizeArgsAsCopy(locale, args));
  }

  @NotNull
  @Override
  public List<Component> asComponents(@NotNull Locale locale) {
    return ComponentFormatter.deserializeLinesAsListWithArgs(valueFunction.apply(locale),
      AbstractMessageKey.localizeArgsAsCopy(locale, args));
  }

  @NotNull
  @Override
  public LocalizableMessage withArgs(@NotNull Object... args) {
    return new DynamicMessageImpl(valueFunction, args);
  }

  @Nullable
  @Override
  public MessageKey getLocalizableKey() {
    return null;
  }

  @NotNull
  @Override
  public Object[] getLocalizableArgs() {
    return args;
  }
}
