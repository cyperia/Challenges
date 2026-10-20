package net.cyperia.challenges.plugin.content.i18n;

import net.cyperia.challenges.plugin.content.i18n.impl.dynamic.DynamicMessageImpl;
import net.cyperia.challenges.plugin.content.i18n.impl.dynamic.JoinedMessageImpl;
import net.cyperia.challenges.plugin.content.i18n.impl.dynamic.WrappedObjMessageImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Represents a {@link MessageKey} with arguments that can be passed as a message argument for dynamic formatting
 * or a {@link MessageKey} instance without arguments.
 *
 * @see MessageKey#withArgs(Object...)
 */
public interface LocalizableMessage {

  @NotNull
  Object[] EMPTY_ARGS = new Object[0];

  @NotNull
  Component asComponent(@NotNull Locale locale);

  @NotNull
  default Component asComponent(@NotNull Player playerLocale) {
    return asComponent(TranslationManager.getLocaleOf(playerLocale));
  }

  @NotNull
  List<Component> asComponents(@NotNull Locale locale);

  @NotNull
  default List<Component> asComponents(@NotNull Player playerLocale) {
    return asComponents(TranslationManager.getLocaleOf(playerLocale));
  }

  /**
   * @return a new {@link LocalizableMessage} instance with the same {@link MessageKey} but with the new provided arguments.
   */
  @NotNull
  @Contract(pure = true)
  LocalizableMessage withArgs(@NotNull Object... args);

  /**
   * @return the {@link MessageKey} of this {@link LocalizableMessage}
   * or {@code null} if this is a dynamic message that does not have a key.
   */
  @Nullable
  MessageKey getLocalizableKey();

  @NotNull
  @ApiStatus.Internal
  Object[] getLocalizableArgs();

  @NotNull
  @CheckReturnValue
  static LocalizableMessage join(@NotNull MessageKey delimiter, @Nullable MessageKey remainingPlaceholder, int limit, @NotNull Object... elements) {
    return new JoinedMessageImpl(delimiter, remainingPlaceholder, limit, elements);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage join(int limit, @NotNull Object... elements) {
    return join(MessageKey.of("arg-format.delimiter"), MessageKey.of("arg-format.remaining"), limit, elements);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage joinArray(int limit, @NotNull Object[] elements) {
    return join(limit, elements);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage joinArray(@NotNull Object[] elements) {
    return join(elements.length, elements);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage joinList(int limit, @NotNull Collection<?> elements) {
    return joinArray(limit, elements.toArray());
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage joinList(@NotNull Collection<?> elements) {
    return joinArray(elements.toArray());
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage joinArgsLimited(int limit, @NotNull Object... elements) { // prevents ambiguous var-args
    return joinArray(limit, elements);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage joinArgs(@NotNull Object... elements) { // prevents ambiguous var-args
    return joinArray(elements);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage fromLines(@NotNull Function<Locale, String[]> valueFunction, @NotNull Object... args) {
    return new DynamicMessageImpl(valueFunction, args);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage from(@NotNull Function<Locale, String> valueFunction, @NotNull Object... args) {
    return fromLines(valueFunction.andThen((string) -> new String[]{string}), args);
  }

  @NotNull
  @CheckReturnValue
  @ApiStatus.Internal
  @ApiStatus.Experimental
  static LocalizableMessage wrap(@NotNull Object arg) {
    if (arg instanceof LocalizableMessage localizable) return localizable;
    return new WrappedObjMessageImpl(arg);
  }

  @NotNull
  @CheckReturnValue
  static LocalizableMessage empty() {
    return WrappedObjMessageImpl.EMPTY;
  }

}
