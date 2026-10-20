package net.cyperia.challenges.plugin;

import net.cyperia.challenges.catalog.ChallengeCatalog;
import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.custom.settings.CustomSettingsLoader;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.content.loader.LoaderRegistry;
import net.cyperia.challenges.plugin.content.loader.UpdateLoader;
import net.cyperia.challenges.plugin.management.blocks.BlockDropManager;
import net.cyperia.challenges.plugin.management.bstats.MetricsLoader;
import net.cyperia.challenges.plugin.management.challenges.ChallengeLoader;
import net.cyperia.challenges.plugin.management.challenges.ChallengeManager;
import net.cyperia.challenges.plugin.management.challenges.CustomChallengesLoader;
import net.cyperia.challenges.plugin.management.challenges.PresetChallengeLoader;
import net.cyperia.challenges.plugin.management.cloud.CloudSupportManager;
import net.cyperia.challenges.plugin.management.database.DatabaseManager;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.inventory.HotbarManager;
import net.cyperia.challenges.plugin.management.menu.MenuManager;
import net.cyperia.challenges.plugin.management.menu.search.SearchRegistry;
import net.cyperia.challenges.plugin.management.scheduler.ScheduleManager;
import net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer;
import net.cyperia.challenges.plugin.management.server.*;
import net.cyperia.challenges.plugin.management.team.TeamProvider;
import net.cyperia.challenges.plugin.spigot.command.*;
import net.cyperia.challenges.plugin.spigot.listener.*;
import net.cyperia.challenges.plugin.utils.bukkit.command.ForwardingCommand;
import net.cyperia.commons.bukkit.core.BukkitModule;
import net.cyperia.commons.common.version.Version;
import org.jetbrains.annotations.NotNull;

@Getter
public final class Challenges extends BukkitModule {

  private static Challenges instance;
  private HotbarManager hotbarManager;
  private ScoreboardManager scoreboardManager;
  private PlayerManager playerManager;
  private ChallengeManager challengeManager;
  private BlockDropManager blockDropManager;
  private ChallengeLoader challengeLoader;
  private CustomChallengesLoader customChallengesLoader;
  private PresetChallengeLoader presetChallengeLoader;
  private CustomSettingsLoader customSettingsLoader;
  private DatabaseManager databaseManager;
  private CloudSupportManager cloudSupportManager;
  private ServerManager serverManager;
  private AutoResetManager autoResetManager;
  private ConfigManager configManager;
  private ScheduleManager scheduler;
  private WorldManager worldManager;
  private BorderManager borderManager;
  private TitleManager titleManager;
  private MenuManager menuManager;
  private ChallengeTimer challengeTimer;
  private LoaderRegistry loaderRegistry;
  private MetricsLoader metricsLoader;
  private GameWorldStorage gameWorldStorage;
  private GeneratorWorldPortalManager generatorWorldPortalManager;
  private TeamProvider teamProvider;
  private TranslationManager translationManager;
  private ChallengeCatalog catalog;
  private SearchRegistry searchRegistry;

  @NotNull
  public static Challenges getInstance() {
    return instance;
  }

  @Override
  protected void handleLoad() {
    checkConfig();
    createManagers();
    loadManagers();
  }

  private void checkConfig() {
    if (!getDataFile("hotbar.yml").exists()) {
      saveResource("hotbar.yml", false);
    }
    if (getConfigDocument().getVersion(ConfigManager.Keys.CONFIG_VERSION, Version.parse("1.0")).isOlderThan(Version.parse("2.0"))) {
      saveResource("config.yml", true);
      reloadConfig();
      getLogger().info("A deprecated config was found and replaced with a new one");
    }
  }

  @Override
  protected void handleEnable() {
    enableManagers();

    registerCommands();
    registerListeners();
  }

  // A module's catalog has to join before the module registers its challenges, so they find their entries
  public void extendCatalog(@NotNull ChallengeCatalog module) {
    catalog = catalog.merge(module).requireComplete();
  }

  private void createManagers() {
    catalog = ChallengeCatalog.load(getClass().getClassLoader()).requireComplete();
    configManager = new ConfigManager();
    configManager.loadConfigs();

    loaderRegistry = new LoaderRegistry(
      new LanguageLoader(),
      new UpdateLoader()
    );

    searchRegistry = new SearchRegistry();
    databaseManager = new DatabaseManager();
    worldManager = new WorldManager();
    borderManager = new BorderManager();
    serverManager = new ServerManager();
    autoResetManager = new AutoResetManager();
    scheduler = new ScheduleManager();
    playerManager = new PlayerManager();
    scoreboardManager = new ScoreboardManager();
    cloudSupportManager = new CloudSupportManager();
    titleManager = new TitleManager();
    challengeTimer = new ChallengeTimer();
    blockDropManager = new BlockDropManager();
    teamProvider = new TeamProvider();
    challengeManager = new ChallengeManager();
    challengeLoader = new ChallengeLoader();
    customChallengesLoader = new CustomChallengesLoader();
    presetChallengeLoader = new PresetChallengeLoader();
    customSettingsLoader = new CustomSettingsLoader();
    menuManager = new MenuManager();
    hotbarManager = new HotbarManager();
    metricsLoader = new MetricsLoader();
    gameWorldStorage = new GameWorldStorage();
    generatorWorldPortalManager = new GeneratorWorldPortalManager();
    translationManager = new TranslationManager();
  }

  private void loadManagers() {
    loaderRegistry.load();
    worldManager.load();
    challengeTimer.loadSession();
  }

  private void enableManagers() {
    gameWorldStorage.enable();
    // Before challengeLoader: the presets it registers resolve their trigger and action from here.
    customSettingsLoader.enable();
    challengeLoader.enable();
    borderManager.enable();
    databaseManager.enable();
    worldManager.enable();
    challengeTimer.enable();
    challengeManager.enable();
    scheduler.start();
    metricsLoader.start();

    loaderRegistry.enable();
  }

  private void registerCommands() {
    registerCommand(new HelpCommand(), "help");
    registerCommand(new ChallengesCommand(), "challenges");
    registerCommand(new TimerCommand(), "timer");
    registerCommand(new ForwardingCommand("timer start"), "start");
    registerCommand(new ForwardingCommand("timer pause"), "pause");
    registerCommand(new ResetCommand(), "reset");
    registerCommand(new DatabaseCommand(), "database");
    registerCommand(new GamestateCommand(), "gamestate");
    registerCommand(new VillageCommand(), "village");
    registerCommand(new HealCommand(), "heal");
    registerCommand(new FeedCommand(), "feed");
    registerCommand(new SearchCommand(), "search");
    registerCommand(new RandomizerCommand(), "randomizer");
    registerCommand(new GoalCommand(), "goal");
    registerListenerCommand(new InvseeCommand(), "invsee");
    registerCommand(new FlyCommand(), "fly");
    registerCommand(new WorldCommand(), "world");
    registerListenerCommand(new BackCommand(), "back");
    registerCommand(new GamemodeCommand(), "gamemode");
    registerCommand(new ForwardingCommand("gamemode 0", false), "gms");
    registerCommand(new ForwardingCommand("gamemode 1", false), "gmc");
    registerCommand(new ForwardingCommand("gamemode 2", false), "gma");
    registerCommand(new ForwardingCommand("gamemode 3", false), "gmsp");
    registerCommand(new WeatherCommand(), "weather");
    registerCommand(new ForwardingCommand("weather sun"), "sun");
    registerCommand(new ForwardingCommand("weather rain"), "rain");
    registerCommand(new ForwardingCommand("weather thunder"), "thunder");
    registerCommand(new TimeCommand(), "time");
    registerCommand(new ForwardingCommand("time set day"), "day");
    registerCommand(new ForwardingCommand("time set night"), "night");
    registerCommand(new ForwardingCommand("time set noon"), "noon");
    registerCommand(new ForwardingCommand("time set midnight"), "midnight");
    registerCommand(new ResultCommand(), "result");
    registerCommand(new SkipTimerCommand(), "skiptimer");
    registerListenerCommand(new GodModeCommand(), "godmode");
    registerCommand(new CancelChatInputCommand(), "cancelinput");
    registerCommand(new TeamsCommand(), "teams");
  }

  private void registerListeners() {
    registerListener(
      new PlayerConnectionListener(),
      autoResetManager,
      new RestrictionListener(),
      new ExtraWorldRestrictionListener(),
      new CheatListener(),
      new BlockDropListener(),
      new CustomEventListener(),
      new HelpListener(),
      new GeneratorWorldsListener(),
      new ScoreboardUpdateListener()
    );
  }

  @Override
  protected void handleDisable() {
    boolean shutdownBecauseOfReset = worldManager != null && worldManager.isShutdownBecauseOfReset();
    boolean restoreDefaultsOnReset = getConfigDocument().getBoolean(ConfigManager.Keys.RESTORE_DEFAULTS_ON_RESET);

    if (hotbarManager != null) hotbarManager.handleDisable();
    if (challengeTimer != null && !shutdownBecauseOfReset) challengeTimer.saveSession(false);
    if (scheduler != null) scheduler.stop();
    if (loaderRegistry != null) loaderRegistry.disable();
    if (databaseManager != null) databaseManager.disconnectIfConnected();
    if (scoreboardManager != null) scoreboardManager.disable();

    if (challengeManager != null) {
      challengeManager.shutdownChallenges();
      if (shutdownBecauseOfReset && restoreDefaultsOnReset) {
        challengeManager.restoreDefaults();
      }
      challengeManager.saveLocalSettings(false);
      challengeManager.saveLocalCustomChallenges(false);

      if (!shutdownBecauseOfReset) {
        challengeManager.saveGamestate(false);
      }
      challengeManager.clearChallengeCache();
    }
  }

}
