package net.cyperia.challenges.plugin.content.i18n.impl.format;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Server side translations for values minecraft cannot translate on its own, i.e. values that do not
 * implement {@link net.kyori.adventure.translation.Translatable} and therefore have no client side
 * translation key. Currently that is only {@link DamageCause}.
 * <p>
 * Translated values are picked up <b>automatically</b> whenever they are passed as a message argument
 * (see {@link net.cyperia.challenges.plugin.content.i18n.impl.MessageKeyImpl#localizeArgs}), so call
 * sites should hand over the value itself instead of a pre-formatted name:
 * <pre>{@code
 * messageKey.send(player, prefix, event.getCause()); // sends the translated damage cause
 * }</pre>
 * Values without a translation degrade gracefully to their prettified constant name
 * ({@code ENTITY_ATTACK -> "Entity Attack"}), so a minecraft update introducing new constants can
 * never break a message.
 *
 * @see net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentArguments#convertToComponent(Object)
 */
public final class CustomTranslatable {

  private static final String KEY_PREFIX = "translatable.";

  private static final String[] DAMAGE_CAUSE_KEYS = keysOf("damage-cause", DamageCause.values());

  private CustomTranslatable() {
  }

  /**
   * @return the message key holding the translation of {@code value}, or {@code null} if it has none,
   * either because its type is not translated server side or because no language provides the key
   */
  @Nullable
  public static MessageKey find(@Nullable Object value) {
    // add a branch here once another type needs a server side translation
    if (value instanceof DamageCause cause) return MessageKey.find(DAMAGE_CAUSE_KEYS[cause.ordinal()]);
    return null;
  }

  /**
   * @return the translation of {@code value}, falling back to the value itself, which the message
   * pipeline renders as its prettified constant name ({@code ENTITY_ATTACK -> "Entity Attack"})
   */
  @NotNull
  public static LocalizableMessage of(@NotNull Object value) {
    MessageKey translation = find(value);
    return translation != null ? translation : LocalizableMessage.wrap(value);
  }

  @NotNull
  private static String[] keysOf(@NotNull String category, @NotNull Enum<?>[] constants) {
    String prefix = KEY_PREFIX + category + ".";
    String[] keys = new String[constants.length];
    for (int i = 0; i < constants.length; i++) {
      keys[i] = prefix + constants[i].name().toLowerCase(Locale.ROOT);
    }
    return keys;
  }

}
