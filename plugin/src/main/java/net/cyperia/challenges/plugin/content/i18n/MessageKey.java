package net.cyperia.challenges.plugin.content.i18n;

import net.cyperia.challenges.catalog.MessageFormatter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.impl.MessageKeyImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static net.cyperia.challenges.plugin.content.i18n.TranslationManager.getLocaleOf;

/**
 * Identifies a translatable message using a unique name (key).
 * Subdocument/-object paths may be accessed by using dots as the path delimiter.
 * <p>
 * Provides all required helpers and accessor to utilize them directly like
 * <ul>
 *   <li>{@link #send(Player, Prefix, Object...)} to send this as a chat message to a player in their language</li>
 *   <li>{@link #sendTitle(Player, Object...)} to send this as a title and subtitle to a player in their language, where the first line in the array in the header, and the second the subtitle</li>
 *   <li>{@link #sendActionBar(Player, Object...)} to send this as an action bar to a player, the translation must be one line</li>
 *   <li>{@link #broadcast(Prefix, Object...)} to send this as a chat message to all players in their language</li>
 * </ul>
 * See {@link net.cyperia.challenges.plugin.management.menu.format.ItemFormatter}
 * for creating items with translated names and descriptions.
 *
 * @implSpec Do <b>NOT</b> store {@link MessageKey} instances {@code statically}, but rather use {@link #of(String)} to retrieve them when needed.
 * @implNote Messages may use the following formats:
 * <ul>
 *  <li>
 *    <b>Positional Arguments:</b>
 *    {@code {0}}, {@code {1}}, etc. - Replaced by corresponding arguments passed at runtime.
 *  </li>
 *  <li>
 *    <b>Message References:</b>
 *    {@code {@message.key}} - Dynamically embeds another message from the bundle.
 *    Supports multi-line resolution and gracefully handles cyclic dependencies.
 *    Positional arguments will be added to the root translation while the argument indices remain unmodified.
 *  </li>
 *  <li>
 *    <b>Mini Message</b>
 *    The system uses "Mini Messages" internally, by parsing {@code <...>} tags;
 *    see the <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage documentation</a> for more.
 *  </li>
 *  <li>
 *    <b>Supported Argument Types</b>
 *    The following argument types are currently supported by default and will automatically be formatted
 *    (and <b>translated</b> using their namespace key if possible)
 *    <ul>
 *      <li>{@link MessageKey} will automatically be localized into the targeted language
 *      (for static linking use {@code {\@x}} reference)</li>
 *      <li>{@link LocalizableMessage} will automatically be localized into the target language,
 *      it can be used to reference messages dynamically with additional object arguments using {@link MessageKey#withArgs(Object...)}</li>
 *      <li>{@link net.kyori.adventure.translation.Translatable} like {@link org.bukkit.Material}, {@link org.bukkit.entity.EntityType}, {@link org.bukkit.GameMode}</li>
 *      <li>Types that don't have a minecraft client-side translation but are supported by
 *      {@link net.cyperia.challenges.plugin.content.i18n.impl.format.CustomTranslatable} will automatically be translated,
 *      when there is a translation available</li>
 *      <li>{@link org.bukkit.entity.Player} using their name</li>
 *      <li>Unknown Enums will be formatted as {@code TEST_ENUM -> "Test Enum"}</li>
 *      <li>Unresolvable types will be serialized using {@link Object#toString()}</li>
 *    </ul>
 *  </li>
 * </ul>
 * @see TranslationManager TranslationManager for localization
 * @see MessageFormatter MessageFormatter for references
 */
public interface MessageKey extends KeyedLocalizableMessage, MessageKeySpace {

  /**
   * @implSpec Do <b>NOT</b> store {@link MessageKey} instances {@code statically}, but rather retrieve them when needed.
   * @see #withArgs(Object...)
   */
  @NotNull
  @CheckReturnValue
  static MessageKey of(@NotNull String id) {
    return TranslationManager.find().getMessageKey(id);
  }

  @NotNull
  @CheckReturnValue
  static MessageKey of(@NotNull String parent, @NotNull String sub) {
    // direct call keeps the stack trace clean
    return TranslationManager.find().getMessageKey(parent + DELIMITER + sub);
  }

  @NotNull
  @CheckReturnValue
  static MessageKey of(@NotNull String parent, @NotNull String subParent, @NotNull String subPath) {
    // direct call keeps the stack trace clean
    return TranslationManager.find().getMessageKey(parent + DELIMITER + subParent + DELIMITER + subPath);
  }

  /**
   * Looks up an already loaded message <b>without</b> registering a placeholder for or warning about
   * an unknown key, which makes it the right choice for optional keys (e.g. keys derived from an enum
   * constant a future minecraft version might add).
   */
  @Nullable
  @CheckReturnValue
  static MessageKey find(@NotNull String id) {
    TranslationManager i18n = Challenges.getInstance().getTranslationManager();
    if (i18n == null) return null;
    return i18n.findMessageKey(id);
  }

  @NotNull
  @CheckReturnValue
  static MessageKey pluralize(@NotNull String singularKey, @NotNull String pluralKey, @NotNull Number count) {
    return of(count.intValue() == 1 ? singularKey : pluralKey);
  }

  @NotNull
  @CheckReturnValue
  static MessageKey pluralize(@NotNull String baseKey, @NotNull Number count) {
    return pluralize(baseKey, baseKey + "s", count);
  }

  @NotNull
  @CheckReturnValue
  @ApiStatus.Internal
  static MessageKey empty(@NotNull String id) {
    return new MessageKeyImpl(id);
  }

  @NotNull
  static String formatMissingTranslation(@NotNull String key, @NotNull Locale locale) {
    return String.format("%s/%s", key, locale);
  }

  /** @return whether this key is loaded in any language */
  boolean exists();

  boolean isCached(@NotNull Locale locale);

  void removeValue(@NotNull Locale locale);

  void setValue(@NotNull Locale locale, @NotNull String[] values);

  /**
   * @param locale language to translate to
   * @return the raw value of this translated message or {@link #formatMissingTranslation(String, Locale)}
   * @implSpec the returned array must never be empty
   */
  @NotNull
  String[] localizeRawValue(@NotNull Locale locale);

  /**
   * @param locale language to translate to
   * @return the raw value of this translated message as one string (if it consists of only one line,
   * joined by newlines {@code \n} if multiple lines) or {@link #formatMissingTranslation(String, Locale)}
   */
  @NotNull
  String localizeRawValueAsSingleLine(@NotNull Locale locale);

  /**
   * Renders this message into plain, unformatted text (no colors, no mini message tags, no click or
   * hover events). Intended for the few places that need a {@link String} rather than a
   * {@link Component}, most notably comparing a translated name against user input.
   *
   * @param locale language to translate to
   */
  @NotNull
  String localizePlainText(@NotNull Locale locale, @NotNull Object... args);

  @NotNull
  String[] localizeWithPrimitiveArgs(@NotNull Locale locale, @NotNull Object... args);

  @NotNull
  String localizeWithPrimitiveArgsAsSingleLine(@NotNull Locale locale, @NotNull Object... args);

  @NotNull
  @Contract(pure = true)
  KeyedLocalizableMessage withArgs(@NotNull Object... args);

  @NotNull
  @Contract(pure = true)
  default KeyedLocalizableMessage withArgs(@NotNull Supplier<?> args) {
    return withArgs((Object) args);
  }

  // Helpers

  /**
   * If the target ({@link CommandSender}) is not an instance of {@link Player},
   * the message will be translated using the global server language set in the {@code plugin.yml}
   *
   * @see net.cyperia.challenges.plugin.content.loader.LanguageLoader#getConfigLanguage()
   */
  void send(@NotNull CommandSender target, @Nullable Prefix prefix, @NotNull Object... args);

  void send(@NotNull Player target, @Nullable Prefix prefix, @NotNull Object... args);

  void broadcast(@Nullable Prefix prefix, @NotNull Object... args);

  void sendTitle(@NotNull Player target, @NotNull Object... args);

  void sendTitleInstantly(@NotNull Player target, @NotNull Object... args);

  void broadcastTitle(@NotNull Object... args);

  void broadcastTitleInstantly(@NotNull Object... args);

  void sendActionBar(@NotNull Player target, @NotNull Object... args);

  void broadcastActionBar(@NotNull Object... args);

  /** Like {@link #broadcastActionBar(Object...)}, but only to the given players instead of the whole audience. */
  void broadcastActionBar(@NotNull Collection<? extends Player> targets, @NotNull Object... args);

  // Formatting

  @NotNull
  Component asComponent(@NotNull Locale locale, @Nullable Prefix prefix, @NotNull Object... args);

  @NotNull
  default Component asComponent(@NotNull Player playerLocale, @Nullable Prefix prefix, @NotNull Object... args) {
    return asComponent(getLocaleOf(playerLocale), prefix, args);
  }

  @NotNull
  default Component asComponent(@NotNull Locale locale, @NotNull Object... args) {
    return asComponent(locale, null, args);
  }

  @NotNull
  default Component asComponent(@NotNull Player playerLocale, @NotNull Object... args) {
    return asComponent(getLocaleOf(playerLocale), args);
  }

  @NotNull
  List<Component> asComponents(@NotNull Locale locale, @NotNull Object... args);

  /**
   * The message as chat lines, each with the prefix, to be sent one by one.
   */
  @NotNull
  List<Component> asChatLines(@NotNull Locale locale, @Nullable Prefix prefix, @NotNull Object... args);

  @NotNull
  default List<Component> asComponents(@NotNull Player playerLocale, @NotNull Object... args) {
    return asComponents(getLocaleOf(playerLocale), args);
  }

}
