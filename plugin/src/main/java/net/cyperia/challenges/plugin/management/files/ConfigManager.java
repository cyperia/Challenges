package net.cyperia.challenges.plugin.management.files;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.FileDocument;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.cyperia.commons.common.misc.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ConfigManager {

  private final List<String> missingConfigSettings;
  @Getter
  private FileDocument sessionConfig, gamestateConfig, settingsConfig, customChallengesConfig;

  public ConfigManager() {
    missingConfigSettings = new LinkedList<>();
    GsonDocument.setCleanupEmptyObjects(true);
    GsonDocument.setCleanupEmptyArrays(true);
  }

  public void loadConfigs() {
    sessionConfig = load("internal/session.json");
    gamestateConfig = load("internal/gamestate.json");
    settingsConfig = load("internal/settings.json");
    customChallengesConfig = load("internal/custom_challenges.json");

    Challenges plugin = Challenges.getInstance();
    Document config = plugin.getConfigDocument();
    Document defaultConfig = plugin.getDefaultConfigDocument();

    if (defaultConfig != null) {
      for (String key : defaultConfig.deepKeys()) {
        if (!config.contains(key)) {
          missingConfigSettings.add(key);
        }
      }
    }

  }

  @Nullable
  private FileDocument load(@NotNull String filename) {
    try {
      File file = Challenges.getInstance().getDataFile(filename);
      FileUtils.createFilesIfNecessary(file);
      return FileDocument.readJsonFile(file);
    } catch (Exception ex) {
      Logger.error("Could not load config '{}': {}", filename, ex);
      return null;
    }
  }

  @NotNull
  public List<String> getMissingConfigSettings() {
    return Collections.unmodifiableList(missingConfigSettings);
  }

  /**
   * Key paths of {@code config.yml}. Every read of the plugin config should go through a constant
   * here so a typo becomes a compile error instead of a silently missing value.
   *
   * <p>Paths are absolute and dot-separated, resolved against the root config document. Reading a
   * full path off the root is equivalent to fetching the sub-document and reading the relative key,
   * so intermediate {@code getDocument(...)} calls are unnecessary for plain value reads.
   */
  @UtilityClass
  public class Keys { // type safety
    public final String CONFIG_VERSION = "config-version";

    public final String LANGUAGE = "language";
    public final String SKIP_LANGUAGE_MIGRATION = "language-skip-migration"; // hidden
    public final String SKIP_LANGUAGE_AUTOLOAD = "language-skip-auto-load"; // hidden
    public final String DIRECT_LANGUAGE_PATH = "language-direct-path"; // hidden
    public final String ONLINE_UPDATE = "language-online-update";

    public final String TIMER_GRADIENT_ENABLED = "timer.gradient.enabled";
    public final String TIMER_GRADIENT_DURATION = "timer.gradient.duration";
    public final String TIMER_GRADIENT_DIRECTION = "timer.gradient.direction";
    public final String ENABLE_DEFAULT_START_SOUNDS = "enable-default-start-sounds";
    public final String ENABLE_SPECIFIC_START_SOUNDS = "enable-specific-start-sounds";
    public final String ENABLED_WIN_SOUNDS = "enabled-win-sounds";

    public final String NEW_SUFFIX = "challenge-updates.new.suffix";
    public final String NEW_IN_FRONT = "challenge-updates.new.in-front";
    public final String UPDATED_SUFFIX = "challenge-updates.updated.suffix";
    public final String UPDATED_IN_FRONT = "challenge-updates.updated.in-front";
    public final String MANAGE_SETTINGS_PERMISSION = "manage-settings-permission";

    public final String ANIMATE_MAIN_MENU = "animate-main-menu";
    public final String PAUSED_PARTICLES = "play-paused-particles";

    public final String TITLES_TIMER_STATUS = "titles.timer-status";
    public final String TITLES_CHALLENGE_STATUS = "titles.challenge-status";

    public final String IGNORE_CREATIVE_PLAYERS = "ignore-players.creative";
    public final String IGNORE_SPECTATOR_PLAYERS = "ignore-players.spectator";

    public final String CUSTOM_SEED_COMMAND = "custom-seed.command";
    public final String CUSTOM_SEED_CONFIG = "custom-seed.config";
    public final String CUSTOM_SEED_VALUE = "custom-seed.seed";

    public final String CHALLENGE_SETTINGS_POSITION_PARTICLE_LINES = "challenge-settings.position-particle-lines";
    public final String CHALLENGE_SETTINGS_BACKPACK_SIZE = "challenge-settings.backpack-size";
    public final String CHALLENGE_SETTINGS_ALL_BLOCKS_DISAPPEAR_STACK_DROP_LIMIT = "challenge-settings.all-block-disappear-stack-drop-limit";

    public final String CUSTOM_CHALLENGE_MAX_NAME_LENGTH = "custom-challenge-settings.max-name-length";
    public final String CUSTOM_CHALLENGE_MAX_COMMAND_LENGTH = "custom-challenge-settings.max-command-length";
    public final String CUSTOM_CHALLENGE_ALLOWED_COMMANDS = "custom-challenge-settings.allowed-commands-to-execute";

    public final String JOIN_QUIT_MESSAGES = "join-quit-messages";
    public final String UNSUPPORTED_VERSION_CHAT_WARNING = "unsupported-version-chat-warning";
    public final String TIMER_COLORS = "timer-colors";
    public final String TIMER_IS_PAUSED_INFO = "timer-is-paused-info";
    public final String START_ON_FIRST_JOIN = "start-on-first-join";
    public final String PAUSE_ON_LAST_LEAVE = "pause-on-last-leave";
    public final String RESET_ON_LAST_LEAVE = "reset-on-last-leave";
    public final String RESTORE_DEFAULTS_ON_LAST_LEAVE = "restore-defaults-on-last-leave";

    public final String SPECTATE_ON_LATE_JOIN = "spectate-on-late-join";
    public final String AUTO_RESET_AFTER_GOAL = "auto-reset.after-goal-seconds";
    public final String AUTO_RESET_AFTER_EMPTY = "auto-reset.after-empty-seconds";

    public final String CONFIRM_RESET = "confirm-reset";
    public final String ENABLE_FRESH_RESET = "enable-fresh-reset";
    public final String RESTART_ON_RESET = "restart-on-reset";
    public final String RESTORE_DEFAULTS_ON_RESET = "restore-defaults-on-reset";
    public final String SET_SPECTATOR_ON_WIN = "set-spectator-on-win";
    public final String DROP_ITEMS_ON_END = "drop-items-on-end";

    public final String CLOUD_SUPPORT_TYPE = "cloud-support.type";
    public final String CLOUD_SUPPORT_SET_INGAME = "cloud-support.set-ingame";
    public final String CLOUD_SUPPORT_START_NEW_SERVICE = "cloud-support.start-new-service";
    public final String CLOUD_SUPPORT_RESET_TO_LOBBY = "cloud-support.reset-to-lobby";

    public final String SAVE_PLAYER_CONFIGS = "save-player-configs";
    public final String SAVE_PLAYER_CUSTOM_CHALLENGES = "save-player-custom_challenges";
    public final String SAVE_PLAYER_STATS = "save-player-stats";
    public final String NO_STATS_AFTER_CHEATING = "no-stats-after-cheating";

    public final String DATABASE_SECTION = "database"; // Section rather than a value: the sub-document is handed to {@code DatabaseConfig}.
    public final String DATABASE_TYPE = "database.type";

    @Deprecated
    public final String DESIGN_EMPTY_LINE_ABOVE = "design.empty-line-above"; // legacy, absent from config.yml
    @Deprecated
    public final String DESIGN_EMPTY_LINE_UNDERNEATH = "design.empty-line-underneath"; // legacy, absent from config.yml
  }

}
