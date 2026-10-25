package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.content.loader.UpdateLoader;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.server.PlayerManager;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.utils.misc.DatabaseHelper;
import net.cyperia.challenges.plugin.utils.misc.ParticleUtils;
import net.cyperia.commons.bukkit.utils.misc.MinecraftVersion;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class PlayerConnectionListener implements Listener {

  private final boolean messages;
  private final boolean timerPausedInfo;
  private final boolean startTimerOnJoin;
  private final boolean resetOnLastQuit;
  private final boolean restoreDefaultsOnLastQuit;
  private final boolean unsupportedVersionWarning;

  public PlayerConnectionListener() {
    Document config = Challenges.getInstance().getConfigDocument();
    messages = config.getBoolean(ConfigManager.Keys.JOIN_QUIT_MESSAGES);
    timerPausedInfo = config.getBoolean(ConfigManager.Keys.TIMER_IS_PAUSED_INFO);
    startTimerOnJoin = config.getBoolean(ConfigManager.Keys.START_ON_FIRST_JOIN);
    resetOnLastQuit = config.getBoolean(ConfigManager.Keys.RESET_ON_LAST_LEAVE);
    restoreDefaultsOnLastQuit = config.getBoolean(ConfigManager.Keys.RESTORE_DEFAULTS_ON_LAST_LEAVE);
    unsupportedVersionWarning = !Challenges.getInstance().isVersionSupported()
      && config.getBoolean(ConfigManager.Keys.UNSUPPORTED_VERSION_CHAT_WARNING, true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    Player player = event.getPlayer();
    boolean playerVisible = Challenges.getInstance().getPlayerManager().isVisible(player);

    player.getLocation().getChunk().load(true);
    if (!isWatching(player)) {
      ParticleUtils.spawnJoinEffect(Challenges.getInstance(), player);
    }
    Challenges.getInstance().getScoreboardManager().handleJoin(player);

    // First thing in chat, so it reads as a banner the rest of the join messages follow rather
    // than something interrupting them. No separator needed: each of those prepends its own.
    sendPluginInfo(player);

    if (Challenges.getInstance().isFirstInstall() && !player.hasPermission(ChallengesPermissions.COMMAND_CHALLENGES)) {
      MessageKey.of("server.not-op").send(player, Prefix.CHALLENGES);
    }

    if (player.hasPermission(ChallengesPermissions.COMMAND_CHALLENGES)) {
      if (Challenges.getInstance().isFirstInstall()) {
        player.sendMessage("");
        HelpListener.sendFirstInstallWelcome(player);

        if (Challenges.getInstance().getTranslationManager().getLanguageProvider().isDefaultBehaviour() && playerVisible) {
          autoDetectPluginLanguage(player);
        }
      }

      if (timerPausedInfo && !startTimerOnJoin && ChallengeAPI.isPaused()) {
        player.sendMessage("");
        MessageKey.of("timer.messages.paused-reminder").send(player, Prefix.CHALLENGES);
      }
    }

//    if (Challenges.getInstance().getServerManager().hasCheated()) { // TODO new stats system: was gated on isNoStatsAfterCheating
//      player.sendMessage("");
//      MessageKey.of("server.cheats-already-detected").send(player, Prefix.CHALLENGES);
//    }


    if (startTimerOnJoin && playerVisible) {
      player.sendMessage("");
      ChallengeAPI.resumeTimer();
    }

    if (player.hasPermission(ChallengesPermissions.COMMAND_CHALLENGES)) {
      int replacedMessages = Challenges.getInstance().getLoaderRegistry().getFirstLoaderByClass(LanguageLoader.class)
        .map(LanguageLoader::takeUnannouncedReplacements)
        .orElse(0);
      if (replacedMessages > 0) {
        player.sendMessage("");
        MessageKey.of("server.messages-replaced").send(player, Prefix.CHALLENGES, replacedMessages, "plugins/Challenges/messages/backups");
      }

      if (!UpdateLoader.isNewestConfigVersion()) {
        player.sendMessage("");
        MessageKey.of("server.config.outdated").send(player, Prefix.CHALLENGES, UpdateLoader.getDefaultConfigVersion().format(), UpdateLoader.getCurrentConfigVersion().format());
      }

      List<String> missingConfigSettings = Challenges.getInstance().getConfigManager().getMissingConfigSettings();
      if (!missingConfigSettings.isEmpty()) {
        player.sendMessage("");
        MessageKey.of("server.config.missing").send(player, Prefix.CHALLENGES, LocalizableMessage.joinList(missingConfigSettings));
      } else if (!UpdateLoader.isNewestConfigVersion()) {
        player.sendMessage("");
        MessageKey.of("server.config.none-missing").send(player, Prefix.CHALLENGES, UpdateLoader.getDefaultConfigVersion().format());
      }
      if (!UpdateLoader.isNewestPluginVersion()) {
        player.sendMessage("");
        MessageKey.of("server.update-available").send(player, Prefix.CHALLENGES, UpdateLoader.DOWNLOAD);
      }
    }

    if (messages || !playerVisible) {
      event.joinMessage(null);
    }
    if (messages && playerVisible) {
      player.sendMessage("");
      MessageKey.of("server.join").broadcast(Prefix.CHALLENGES, event.getPlayer());
    }

    if (Challenges.getInstance().getDatabaseManager().isConnected()) {
      Challenges.getInstance().runAsync(() -> DatabaseHelper.savePlayerData(player));
    }

    if (unsupportedVersionWarning) {
      // A tick later, after the join messages of every other plugin, so it is the last thing in chat
      Regions.nextTick(Challenges.getInstance(), player, () -> {
        player.sendMessage("");
        MessageKey.of("server.unsupported-version").send(player, Prefix.CHALLENGES,
          Challenges.getInstance().getDescription().getVersion(),
          Challenges.getInstance().getPluginDocument().getDocument("require").getVersion("version").format(),
          MinecraftVersion.current().format());
      });
    }

  }

  /**
   * @implNote The quitting player is still online while {@link PlayerQuitEvent} is handled, so they
   * are excluded explicitly. Vanished players never count as somebody being left on the server.
   */
  private static boolean isLastVisiblePlayer(@NotNull Player quitting) {
    PlayerManager playerManager = Challenges.getInstance().getPlayerManager();
    for (Player player : playerManager.getAudience()) {
      if (player != quitting && playerManager.isVisible(player)) return false;
    }
    return true;
  }

  // The public release cannot turn the credits off, its license forbids hiding them
  private void sendPluginInfo(@NotNull Player player) {
    if (Challenges.getInstance().isFirstInstall()) {
      HelpListener.sendPluginInfo(player);
    } else {
      HelpListener.sendPluginSummary(player);
    }
  }

  // A late joiner still reports a playing game mode here: AutoResetManager applies spectator at
  // MONITOR, after this listener, so they have to be asked about rather than looked at.
  private static boolean isWatching(@NotNull Player player) {
    return Challenges.getInstance().getPlayerManager().isIgnored(player)
      || Challenges.getInstance().getAutoResetManager().isLateJoinSpectator(player);
  }

  private static void autoDetectPluginLanguage(@NotNull Player player) {
    Challenges.getInstance().runAsync(() -> {
      Locale playerLocale = player.locale();
      Challenges.getInstance().getLoaderRegistry().getFirstLoaderByClass(LanguageLoader.class).ifPresent(languageLoader -> {
        Locale locale = languageLoader.getAvailableLocaleTags().get(playerLocale.getLanguage());
        if (locale == null) return; // player locale not available for Challenges
        String languageTag = locale.getLanguage();
        if (languageTag.equals(languageLoader.getConfigLanguageTag())) return; // already set to this language
        languageLoader.changeLanguage(languageTag);
        MessageKey.of("server.language-autodetect").broadcast(Prefix.CHALLENGES, MessageKey.of("language", languageTag), languageTag);
      });
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onQuit(@NotNull PlayerQuitEvent event) {

    try {
      Player player = event.getPlayer();
      Challenges.getInstance().getScoreboardManager().handleQuit(player);
      DatabaseHelper.clearCache(event.getPlayer().getUniqueId());

      if (Challenges.getInstance().getWorldManager().isShutdownBecauseOfReset()) {
        event.quitMessage(null);
      } else if (messages) {
        event.quitMessage(null);
        if (Challenges.getInstance().getPlayerManager().isVisible(player)) {
          MessageKey.of("server.quit").broadcast(Prefix.CHALLENGES, player);
        }
      }
    } catch (Exception exception) {
      Challenges.getInstance().getILogger().error("Error while handling disconnect", exception);
    }

    if (isLastVisiblePlayer(event.getPlayer())) {

      if (!Challenges.getInstance().getWorldManager().isShutdownBecauseOfReset()) {
        if (resetOnLastQuit && !ChallengeAPI.isFresh()) {
          Challenges.getInstance().getWorldManager().prepareWorldReset(Bukkit.getConsoleSender());
          return;
        }
      }

      if (restoreDefaultsOnLastQuit) {
        Challenges.getInstance().getChallengeManager().restoreDefaults();
      }
    }

  }

}
