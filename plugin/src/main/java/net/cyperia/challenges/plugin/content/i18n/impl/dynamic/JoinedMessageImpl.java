package net.cyperia.challenges.plugin.content.i18n.impl.dynamic;

import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.AbstractMessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentArguments;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentSplitter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class JoinedMessageImpl implements LocalizableMessage {

  private final MessageKey delimiter;
  private final MessageKey remainingPlaceholder;
  private final int limit;
  private final Object[] elements;

  @NotNull
  @Override
  public Component asComponent(@NotNull Locale locale) {
    if (elements.length == 0) return Component.empty(); // nothing to join

    int shown = Math.min(elements.length, limit);
    int remaining = elements.length - shown;

    List<Component> parts = new ArrayList<>(Math.max(shown * 2, 1)); // elements, separators and the remaining placeholder
    Component localizedDelimiter = null;
    for (int i = 0; i < shown; i++) {
      if (i > 0) {
        if (localizedDelimiter == null) localizedDelimiter = delimiter.asComponent(locale);
        parts.add(localizedDelimiter);
      }
      parts.add(ComponentArguments.convertToComponent(AbstractMessageKey.localizeArg(locale, elements[i])));
    }
    if (remaining > 0 && remainingPlaceholder != null) {
      parts.add(remainingPlaceholder.asComponent(locale, remaining));
    }

    return ComponentFormatter.applyDefaultStyle(Component.empty().children(parts));
  }

  @NotNull
  @Override
  public List<Component> asComponents(@NotNull Locale locale) {
    return ComponentSplitter.split(asComponent(locale));
  }

  @NotNull
  @Override
  public LocalizableMessage withArgs(@NotNull Object... args) {
    return new JoinedMessageImpl(delimiter, remainingPlaceholder, limit, args);
  }

  @Nullable
  @Override
  public MessageKey getLocalizableKey() {
    return null;
  }

  @NotNull
  @Override
  public Object[] getLocalizableArgs() {
    return elements;
  }
}
