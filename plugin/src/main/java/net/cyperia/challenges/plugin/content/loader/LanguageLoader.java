package net.cyperia.challenges.plugin.content.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.content.i18n.hook.LanguageProvider;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.utils.logging.ConsolePrint;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.collection.IOUtils;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.misc.FileUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class LanguageLoader extends ContentLoader {

  private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

  public static final String FALLBACK_LANGUAGE = TranslationManager.FALLBACK_LOCALE.getLanguage();

  public static final String GLOBAL_TAG = "global";

  @Getter
  private static volatile boolean loaded = false;
  @Getter
  private String configLanguageTag; // default, will be ignored if custom LanguageProvider is set in TranslationManager
  @Getter
  private Locale configLanguage; // default, will be ignored if custom LanguageProvider is set in TranslationManager
  @Getter
  private boolean onlineUpdateEnabled;
  @Getter
  private boolean skipMigration;
  @Getter
  private boolean skipAutoLoad;
  @Getter
  private String directPath;
  @Getter
  private Map<String, Locale> availableLocaleTags;
  private Set<String> loadedLanguages;

  @Override
  protected void load() {
    Document config = Challenges.getInstance().getConfigDocument();

    onlineUpdateEnabled = config.getBoolean(ConfigManager.Keys.ONLINE_UPDATE, false);
    configLanguageTag = config.getString(ConfigManager.Keys.LANGUAGE, FALLBACK_LANGUAGE);
    skipMigration = config.getBoolean(ConfigManager.Keys.SKIP_LANGUAGE_MIGRATION, false);
    skipAutoLoad = config.getBoolean(ConfigManager.Keys.SKIP_LANGUAGE_AUTOLOAD, false);
    directPath = config.getString(ConfigManager.Keys.DIRECT_LANGUAGE_PATH);
    configLanguage = Locale.forLanguageTag(configLanguageTag);
    availableLocaleTags = new HashMap<>();
    loadedLanguages = new HashSet<>();

    Logger.debug("LanguageLoader initialized with language '{}', online update enabled: {}, skip migration: {}, skip auto load: {} direct path: {}",
      configLanguageTag, onlineUpdateEnabled, skipMigration, skipAutoLoad, directPath);

    // extracted the language provider so it can easily be overridden
    Challenges.getInstance().getTranslationManager().setLanguageProvider(new DefaultLanguageProvider());

    loadDefault();
  }

  private void loadDefault() {
    if (skipMigration) {
      for (String language : loadBundledLanguageNames()) {
        if (language.equals(GLOBAL_TAG)) continue;
        availableLocaleTags.put(language, Locale.forLanguageTag(language));
      }
    } else {
      migrateLanguages();
      if (onlineUpdateEnabled) {
        pullNewOnlineLanguages();
      }
      discoverAvailableLanguageTagFiles();
    }

    if (!skipAutoLoad) {
      readSelectedLanguageIfNotPresent();
      callSubscribers();
    }
  }

  public void changeLanguage(@NotNull String language) {
    if (language.equalsIgnoreCase(this.configLanguageTag)) {
      Logger.info("Language '{}' is already selected", language);
      return;
    }

    Challenges.getInstance().setValueInConfig(ConfigManager.Keys.LANGUAGE, language);
    reloadWithLanguage(language);
    Logger.info("Language changed to '{}'", language);
  }

  private void reloadWithLanguage(@NotNull String language) {
    this.configLanguageTag = language;
    checkLanguageFileExists(); // do the check first, might change languageTag to fallback
    this.configLanguage = Locale.forLanguageTag(configLanguageTag);
    readSelectedLanguageIfNotPresent();
    callSubscribers();
  }

  private void checkLanguageFileExists() {
    File file = getLanguageFile(configLanguageTag);

    if (!file.exists() && !skipMigration) {
      if (configLanguageTag.equalsIgnoreCase(FALLBACK_LANGUAGE)) return;
      ConsolePrint.unknownLanguage(configLanguageTag);
      configLanguageTag = FALLBACK_LANGUAGE;
    }

    Logger.debug("Language '{}' is currently selected", configLanguageTag);
  }

  private void readSelectedLanguageIfNotPresent() {
    if (loadedLanguages.contains(configLanguageTag)) return;
    populateLanguageFromSource(configLanguage);
  }

  @NotNull
  @Override
  protected File getMessagesFolder() {
    if (directPath != null) {
      File file = new File(directPath);
      if (!file.isDirectory() || !file.exists()) {
        Logger.warn("Direct language path '{}' does not exists or is not a directory, falling back to default messages folder", directPath);
      } else {
        return file;
      }
    }

    return super.getMessagesFolder();
  }

  private void discoverAvailableLanguageTagFiles() {
    // only .json files supported; global is not a selectable language
    File[] files = getMessagesFolder()
      .listFiles((_, name) -> name.endsWith(".json") && !name.startsWith(GLOBAL_TAG));
    if (files == null) return;
    for (File file : files) {
      if (!file.isFile()) continue;
      String languageTag = FileUtils.getFileName(file);
      availableLocaleTags.put(languageTag, Locale.forLanguageTag(languageTag));
    }
  }

  private void migrateLanguages() {
    // migrate using bundled languages + changed online translations (if enabled)
    for (String language : loadBundledLanguageNames()) {
      try {
        Document bundledDocument = loadBundledLanguageDocument(language);
        if (bundledDocument == null) continue;

        Document migratedDocument = migrateLanguageDocument(bundledDocument, language);
        Document onlineDocument = onlineUpdateEnabled ? fetchOnlineLanguageDocument(language) : null;
        migrateLanguageFileAndSave(migratedDocument, bundledDocument, onlineDocument, language);
        bundledDocument.saveToFile(getBundledSnapshotFile(language));
      } catch (IOException ex) {
        Logger.error("Could not migrate language file for {}", language, ex);
      }
    }
  }

  private void pullNewOnlineLanguages() {
    for (String language : fetchOnlineLanguages()) {
      File file = getLanguageFile(language);
      if (file.exists()) continue; // already exists, was migrated in previous step

      try {
        Document onlineDocument = fetchOnlineLanguageDocument(language);
        if (onlineDocument == null) continue;
        onlineDocument.saveToFile(file);
        Logger.info("Added new language '{}' from online update", language);
      } catch (IOException ex) {
        Logger.error("Could not save new language file for {}", language, ex);
      }
    }
  }

  @NotNull
  @CheckReturnValue
  private Document migrateLanguageDocument(@NotNull Document bundledLanguage, @NotNull String languageName) throws IOException {
    File destinationFile = getLanguageFile(languageName);
    if (!destinationFile.exists()) {
      return bundledLanguage;
    }

    JsonObject extracted = readJson(destinationFile);
    File snapshot = getBundledSnapshotFile(languageName);
    LanguageMigration.Result result = LanguageMigration.migrate(extracted, JsonParser.parseString(bundledLanguage.toJson()).getAsJsonObject(),
      snapshot.exists() ? readJson(snapshot) : null);

    if (result.replacedCount() > 0) {
      File backup = new File(getMessagesFolder(), "backups/" + languageName + "-" + BACKUP_TIME.format(LocalDateTime.now()) + ".json");
      Document.parseJson(result.replaced().toString()).saveToFile(backup);
      Logger.warn("This update replaced {} message(s) in {} that may have been customized, the replaced ones are saved in {}",
        result.replacedCount(), destinationFile.getName(), backup.getPath());
      addUnannouncedReplacements(result.replacedCount());
    }
    return Document.parseJson(extracted.toString());
  }

  // The bundled file the extracted one was last migrated to, which tells a customized message from an outdated one
  @NotNull
  private File getBundledSnapshotFile(@NotNull String languageName) {
    return Challenges.getInstance().getDataFile("internal/bundled-messages/" + languageName + ".json");
  }

  @NotNull
  private static JsonObject readJson(@NotNull File file) throws IOException {
    return JsonParser.parseString(Files.readString(file.toPath())).getAsJsonObject();
  }

  // Kept on disk until an admin was told, so a restart before any admin joins does not lose it
  @NotNull
  private static File getUnannouncedReplacementsFile() {
    return Challenges.getInstance().getDataFile("internal/unannounced-message-replacements.txt");
  }

  private static void addUnannouncedReplacements(int replacements) throws IOException {
    File file = getUnannouncedReplacementsFile();
    int pending = file.exists() ? Integer.parseInt(Files.readString(file.toPath()).trim()) : 0;
    Files.createDirectories(file.toPath().getParent());
    Files.writeString(file.toPath(), String.valueOf(pending + replacements));
  }

  /**
   * How many customized messages updates replaced since an admin was last told, once: the count is cleared by reading it.
   */
  public int takeUnannouncedReplacements() {
    File file = getUnannouncedReplacementsFile();
    if (!file.exists()) return 0;
    try {
      int replacements = Integer.parseInt(Files.readString(file.toPath()).trim());
      Files.delete(file.toPath());
      return replacements;
    } catch (IOException | NumberFormatException ex) {
      Logger.error("Could not read the unannounced message replacements", ex);
      return 0;
    }
  }

  private void migrateLanguageFileAndSave(@NotNull Document migratedLanguage, @NotNull Document bundledLanguage,
                                          @Nullable Document onlineLanguage, @NotNull String languageName) throws IOException {
    File destinationFile = getLanguageFile(languageName);
    if (onlineLanguage == null) {
      migratedLanguage.saveToFile(destinationFile);
      return;
    }

    onlineLanguage.deepKeys().forEach(key -> {
      if (!migratedLanguage.contains(key) || Objects.equals(migratedLanguage.getString(key), bundledLanguage.getString(key))) {
        // if the message is not present or was not customized, overwrite it with the online version (like typo fix)
        Object value = onlineLanguage.getObject(key);
        Logger.debug("Overwriting message {} in {} from online-update with {}", key, languageName, String.valueOf(value).replace("\"", "§r\""));
        migratedLanguage.set(key, value);
      }
    });
    migratedLanguage.saveToFile(destinationFile);
  }

  @NotNull
  private List<String> loadBundledLanguageNames() {
    try (InputStream in = getLanguageResourceStream("languages.json")) {
      if (in == null) throw new IllegalStateException("Could not load bundled languages.json from jar");
      return Document.parseJsonStringArray(IOUtils.toString(in));
    } catch (Exception ex) {
      Logger.error("Could not load bundled languages", ex);
      return Collections.emptyList();
    }
  }

  @Nullable
  private Document loadBundledLanguageDocument(@NotNull String languageName) {
    try (InputStream in = getLanguageResourceStream("locales/" + languageName + ".json")) {
      if (in == null) return null;
      return Document.parseJson(in);
    } catch (Exception ex) {
      Logger.error("Could not load bundled language '{}' from jar", languageName, ex);
      return null;
    }
  }

  @Nullable
  private InputStream getLanguageResourceStream(@NotNull String path) {
    // no leading slash!
    return Challenges.getInstance().getResource("language/" + path);
  }

  private File getLanguageFile(String languageName) {
    return getMessageFile(languageName, "json");
  }

  @NotNull
  private List<String> fetchOnlineLanguages() {
    try {
      return Document.parseJsonStringArray(IOUtils.toString(getGitHubUrl("language/languages.json")));
    } catch (Exception ex) {
      Logger.error("Could not fetch online languages", ex);
      return Collections.emptyList();
    }
  }

  @Nullable
  private Document fetchOnlineLanguageDocument(@NotNull String languageName) {
    String url = getGitHubUrl("language/locales/" + languageName + ".json");
    try {
      return Document.parseJson(IOUtils.toString(url));
    } catch (Exception ex) {
      Logger.error("Could not fetch online language '{}'", languageName, ex);
      return null;
    }
  }

  private void populateLanguageFromSource(@NotNull Locale locale) {
    if (skipMigration && directPath == null) {
      populateLanguageFromBundled(locale);
    } else {
      populateLanguageFromFile(locale);
    }
  }

  private void populateLanguageFromFile(@NotNull Locale locale) {
    populateLanguageFromFile(getLanguageFile(locale.getLanguage()), getLanguageFile(GLOBAL_TAG), locale);
  }

  private void populateLanguageFromBundled(@NotNull Locale locale) {
    Document languageDocument = loadBundledLanguageDocument(locale.getLanguage());
    Document globalDocument = loadBundledLanguageDocument(GLOBAL_TAG);
    if (languageDocument == null || globalDocument == null) return;

    int messageCount = populateLanguageFromDocument(languageDocument, globalDocument, locale);
    loaded = true;
    Logger.info("Successfully loaded language '{}' from bundled resource: {} message(s)", locale, messageCount);
  }

  private void populateLanguageFromFile(@NotNull File languageFile, @NotNull File globalFile, @NotNull Locale locale) {
    try {
      if (!languageFile.exists() || !globalFile.exists()) {
        ConsolePrint.unableToGetLanguages();
        return;
      }

      Document globalDocument = Document.readJsonFile(globalFile);
      Document languageDocument = Document.readJsonFile(languageFile);
      int messageCount = populateLanguageFromDocument(languageDocument, globalDocument, locale);

      loaded = true;
      Logger.info("Successfully loaded language '{}' from config file: {} message(s)", locale, messageCount);
    } catch (Exception ex) {
      Logger.error("Could not read languages", ex);
    }
  }

  private int populateLanguageFromDocument(@NotNull Document languageDocument, @NotNull Document globalDocument, @NotNull Locale locale) {
    loadedLanguages.add(locale.getLanguage());
    return Challenges.getInstance().getTranslationManager().populateLanguageFromDocument(locale, languageDocument, globalDocument);
  }

  public void populateLanguagesFromSource(@NotNull Collection<Locale> locales) {
    for (Locale locale : locales) {
      populateLanguageFromSource(locale);
    }
    callSubscribers();
  }

  public void populateAvailableLanguagesFromSource() {
    for (Locale locale : availableLocaleTags.values()) {
      populateLanguageFromSource(locale);
    }
    callSubscribers();
  }

  public class DefaultLanguageProvider implements LanguageProvider {

    @NotNull
    @Override
    public Locale getPlayerLanguage(@NotNull Player player) {
      return configLanguage;
    }

    @Override
    public boolean isDefaultBehaviour() {
      return true;
    }

    @Override
    public boolean isUserSpecific() {
      return false;
    }
  }

}
