package net.cyperia.challenges.plugin.content.i18n.impl;

import lombok.Getter;
import lombok.Setter;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MessageKeyImpl extends AbstractMessageKey {

  @Getter
  protected final String key;

  @Getter
  @Setter
  protected Map<Locale, String[]> values = new HashMap<>();

  public MessageKeyImpl(@NotNull String key) {
    this.key = key;
  }

  public void setValue(@NotNull Locale locale, @NotNull String[] values) {
    this.values.put(locale, values);
  }

  @Override
  public void removeValue(@NotNull Locale locale) {
    values.remove(locale);
  }

  @Override
  public boolean exists() {
    return !values.isEmpty();
  }

  @Override
  public boolean isCached(@NotNull Locale locale) {
    return values.containsKey(locale);
  }

  @NotNull
  @Override
  public String[] localizeRawValue(@NotNull Locale locale) {
    String[] value = values.get(locale);
    if (value != null && value.length != 0) return value;
    return arrayOf(MessageKey.formatMissingTranslation(key, locale));
  }

  @NotNull
  @Override
  public String localizeRawValueAsSingleLine(@NotNull Locale locale) {
    String[] value = values.get(locale);
    if (value == null || value.length == 0) return MessageKey.formatMissingTranslation(key, locale);
    return asSingleLine(value);
  }

  @Override
  public String toString() {
    return key;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof MessageKeyImpl that)) return false;
    return Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  // NOTE: args must not be localized in place here; callers may hand over the arg array stored in a
  // LocalizableMessage (see ChallengeBossBar), which would pin that message to the first locale it
  // was ever rendered for.

  @NotNull
  @Override
  public Component asComponent(@NotNull Locale locale, @Nullable Prefix prefix, @NotNull Object... args) {
    return ComponentFormatter.deserializeLinesWithArgs(localizePrefix(locale, prefix),
      localizeWithPrimitiveArgs(locale, args), localizeArgsAsCopy(locale, args));
  }

  @NotNull
  @Override
  public List<Component> asChatLines(@NotNull Locale locale, @Nullable Prefix prefix, @NotNull Object... args) {
    return ComponentFormatter.deserializeChatLinesWithArgs(localizePrefix(locale, prefix),
      localizeWithPrimitiveArgs(locale, args), localizeArgsAsCopy(locale, args));
  }

  @NotNull
  @Override
  public List<Component> asComponents(@NotNull Locale locale, @NotNull Object... args) {
    return ComponentFormatter.deserializeLinesAsListWithArgs(localizeWithPrimitiveArgs(locale, args), localizeArgsAsCopy(locale, args));
  }

}
