package net.cyperia.challenges.plugin.content.i18n;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.cyperia.challenges.catalog.Locales;
import net.cyperia.challenges.catalog.MessageFormatter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.hook.LanguageProvider;
import net.cyperia.challenges.plugin.content.i18n.hook.MessageTranslatorDelegate;
import net.cyperia.challenges.plugin.content.i18n.impl.MessageKeyImpl;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.misc.ReflectionUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationManager {

  public static final Locale FALLBACK_LOCALE = Locale.ENGLISH;

  // default: populated by LanguageLoader
  // message name/key -> value obj
  protected final Map<String, MessageKey> cachedMessages = new ConcurrentHashMap<>();
  protected final Set<Locale> cachedLanguages = new HashSet<>();

  protected LanguageProvider languageProvider; // by default injected by LanguageLoader
  protected MessageTranslatorDelegate messageTranslatorDelegate; // by default not set

  @NotNull
  public LanguageProvider getLanguageProvider() {
    if (languageProvider == null)
      throw new IllegalStateException("LanguageProvider not yet set, probably accessed before LanguageLoader has been called!");
    return languageProvider;
  }

  @Nullable
  public MessageTranslatorDelegate getMessageTranslatorDelegate() {
    return messageTranslatorDelegate;
  }

  public boolean isLanguageProviderUninitialized() {
    // will only ever be null during onLoad cycle
    return languageProvider == null;
  }

  /**
   * @return the language {@code player} receives messages in, or {@link #FALLBACK_LOCALE} while the
   * {@link LanguageProvider} is not initialized yet
   */
  @NotNull
  public static Locale getLocaleOf(@NotNull Player player) {
    TranslationManager i18n = Challenges.getInstance().getTranslationManager();
    if (i18n == null || i18n.isLanguageProviderUninitialized()) return FALLBACK_LOCALE;
    return i18n.getLanguageProvider().getPlayerLanguage(player);
  }

  /**
   * @return the global server language set in the {@code plugin.yml}, used for output that is not
   * directed at a single player, or {@link #FALLBACK_LOCALE} while the {@link LanguageLoader} has
   * not been registered yet
   */
  @NotNull
  public static Locale getServerLocale() {
    return Challenges.getInstance().getLoaderRegistry().getFirstLoaderByClass(LanguageLoader.class)
      .map(LanguageLoader::getConfigLanguage)
      .orElse(FALLBACK_LOCALE);
  }

  @NotNull
  static TranslationManager find() {
    TranslationManager instance = Challenges.getInstance().getTranslationManager();
    if (instance == null)
      throw new IllegalStateException("TranslationManager not yet initialized! (probably during onLoad)");
    return instance;
  }

  public void setLanguageProvider(@NotNull LanguageProvider languageProvider) {
    Preconditions.checkNotNull(languageProvider, "Cannot set LanguageProvider to null");
    this.languageProvider = languageProvider;

    // menu visibility may depend on the provider (e.g. LanguageSetting hides itself
    // for non-default providers), so regenerate already-built menus
    Challenges.getInstance().getLoaderRegistry().recallSubscribers(LanguageLoader.class);
  }

  public void setMessageTranslatorDelegate(@Nullable MessageTranslatorDelegate messageTranslatorDelegate) {
    this.messageTranslatorDelegate = messageTranslatorDelegate;

    // reload / populate cached languages from source to resolve reference overwrites correctly
    Challenges.getInstance().getLoaderRegistry().findLoaderByClassOrThrow(LanguageLoader.class)
      .populateLanguagesFromSource(getCachedLanguages());
  }

  @NotNull
  public MessageKey getMessageKey(@NotNull String key) {
    return cachedMessages.computeIfAbsent(key, forKey -> {
      Challenges.getInstance().getILogger().warn("Tried accessing unknown message '{}', called by {}", forKey, ReflectionUtils.getCallerName(3));
      return computeNewMessageKey(forKey);
    });
  }

  @NotNull
  protected MessageKey computeNewMessageKey(@NotNull String key) {
    final MessageKeyImpl virgin = new MessageKeyImpl(key);

    if (messageTranslatorDelegate != null) {
      MessageKey override = messageTranslatorDelegate.getMessageKeyOverride(key, virgin);
      if (override != null) return override;
    }

    return virgin;
  }

  /**
   * Unlike {@link #getMessageKey(String)} this neither registers a placeholder for nor warns about
   * unknown keys, which makes it the right choice for keys that are only optionally translated
   * (e.g. derived from an enum constant that a future minecraft version might add).
   *
   * @return the loaded key or {@code null} if no language provides a value for it
   */
  @Nullable
  @ApiStatus.Internal
  public MessageKey findMessageKey(@NotNull String key) {
    MessageKey message = cachedMessages.get(key);
    return message != null && message.exists() ? message : null;
  }

  @NotNull
  public Set<Locale> getCachedLanguages() {
    return Collections.unmodifiableSet(cachedLanguages);
  }

  public boolean isLanguageCached(@NotNull Locale locale) {
    boolean cachedUnnormalized = cachedLanguages.contains(locale);
    if (cachedUnnormalized) return true;
    return cachedLanguages.contains(normalizeLocale(locale));
  }

  public void unloadLanguage(@NotNull Locale locale) {
    for (MessageKey message : cachedMessages.values()) {
      message.removeValue(locale);
    }
    cachedLanguages.remove(normalizeLocale(locale));
  }

  public int populateLanguageFromDocument(@NotNull Locale locale, @NotNull Document document, @Nullable Document globalDocument) {
    Map<String, String[]> messageBundle = new HashMap<>();

    if (globalDocument != null) {
      extractFlattenedMessagesInto(globalDocument, messageBundle, MessageFormatter.SUB_DOCUMENT_DELIMITER, "");
    }
    extractFlattenedMessagesInto(document, messageBundle, MessageFormatter.SUB_DOCUMENT_DELIMITER, "");

    Document config = Challenges.getInstance().getConfigDocument();
    if (config.contains(ConfigManager.Keys.TIMER_COLORS)) {
      messageBundle.putAll(TimerColors.colorizers(config.getDocument(ConfigManager.Keys.TIMER_COLORS), Logger::warn));
    }

    if (messageTranslatorDelegate != null) {
      applyMessageBundleOverride(locale, messageBundle);
    }

    populateLanguage(locale, messageBundle);
    return messageBundle.size();
  }

  private void extractFlattenedMessagesInto(@NotNull Document from, @NotNull Map<String, String[]> into, char pathDelimiter, @NotNull String path) {
    for (String key : from.keys()) {
      if (from.isDocument(key)) {
        extractFlattenedMessagesInto(from.getDocument(key), into, pathDelimiter, path + key + pathDelimiter);
      } else {
        into.put(path + key, from.getStringArray(key));
      }
    }
  }

  private void applyMessageBundleOverride(@NotNull Locale locale, @NotNull Map<String, String[]> messageBundle) {
    ImmutableSet<String> keys = Sets.union(messageBundle.keySet(), messageTranslatorDelegate.getMessageBundleKeyAdditions()).immutableCopy();
    for (String key : keys) { // iterate over copy so we don't get a CME
      String[] override = messageTranslatorDelegate.getMessageBundleOverride(key, locale);
      if (override != null) {
        messageBundle.put(key, override);
      }
    }
  }

  public void populateLanguage(@NotNull Locale locale, @NotNull Map<String, String[]> messageBundle) {
    // references are resolved once here instead of on every send, which needs the whole bundle up front
    Locales.resolve(messageBundle).forEach((key, value) ->
      cachedMessages.computeIfAbsent(key, this::computeNewMessageKey).setValue(locale, value));
    cachedLanguages.add(normalizeLocale(locale));
  }

  // TODO remove
  @NotNull
  @CheckReturnValue
  public static Locale normalizeLocale(@Nullable Locale locale) {
    if (locale == null) return FALLBACK_LOCALE;

    if (locale.getCountry().isEmpty() &&
      locale.getVariant().isEmpty() &&
      locale.getScript().isEmpty() &&
      locale.getExtensionKeys().isEmpty()) {
      return locale;
    }

    // O(1) impl for supported languages
    return switch (locale.getLanguage()) {
      case "en" -> Locale.ENGLISH;
      case "de" -> Locale.GERMAN;

      // Locale.forLanguagerTag is expensive, use only if necessary
      default -> Locale.forLanguageTag(locale.getLanguage());
    };
  }

}
