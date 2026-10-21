package net.cyperia.challenges.plugin;

import lombok.Getter;
import net.cyperia.challenges.catalog.ChallengeCatalog;
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
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandManager;
import net.cyperia.challenges.plugin.management.database.DatabaseManager;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.inventory.HotbarManager;
import net.cyperia.challenges.plugin.management.menu.MenuManager;
import net.cyperia.challenges.plugin.management.menu.search.SearchRegistry;
import net.cyperia.challenges.plugin.management.scheduler.ScheduleManager;
import net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer;
import net.cyperia.challenges.plugin.management.server.*;
import net.cyperia.challenges.plugin.management.team.TeamProvider;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.spigot.command.*;
import net.cyperia.challenges.plugin.spigot.command.WeatherCommand.Weather;
import net.cyperia.challenges.plugin.spigot.listener.*;
import net.cyperia.commons.bukkit.core.BukkitModule;
import net.cyperia.commons.common.version.Version;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
  private CommandManager commandManager;
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
    commandManager = new CommandManager();
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
    commandManager.enable(this);
    for (ChallengeCommand command : List.of(
      new HelpCommand(),
      new ChallengesCommand(),
      new TimerCommand(),
      ShortcutCommand.forSender(CommandInfo.command("start", ChallengesPermissions.COMMAND_TIMER_START).withAliases("resume"),
        (sender, context) -> TimerCommand.start(sender)),
      ShortcutCommand.forSender(CommandInfo.command("pause", ChallengesPermissions.COMMAND_TIMER_PAUSE),
        (sender, context) -> TimerCommand.pause(sender)),
      new ResetCommand(),
      new DatabaseCommand(),
      new GamestateCommand(),
      new VillageCommand(),
      new HealCommand(),
      new FeedCommand(),
      new SearchCommand(),
      new RandomizerCommand(),
      new GoalCommand(),
      new FlyCommand(),
      new WorldCommand(),
      new GamemodeCommand(),
      new GamemodeShortcutCommand("gms", GameMode.SURVIVAL),
      new GamemodeShortcutCommand("gmc", GameMode.CREATIVE),
      new GamemodeShortcutCommand("gma", GameMode.ADVENTURE),
      new GamemodeShortcutCommand("gmsp", GameMode.SPECTATOR),
      new WeatherCommand(),
      WeatherCommand.shortcut("sun", Weather.CLEAR),
      WeatherCommand.shortcut("rain", Weather.RAIN),
      WeatherCommand.shortcut("thunder", Weather.THUNDER),
      new TimeCommand(),
      TimeCommand.shortcut("day"),
      TimeCommand.shortcut("night"),
      TimeCommand.shortcut("noon"),
      TimeCommand.shortcut("midnight"),
      new ResultCommand(),
      new SkipTimerCommand(),
      new CancelChatInputCommand(),
      new TeamsCommand()
    )) {
      commandManager.register(command);
    }

    InvseeCommand invsee = new InvseeCommand();
    BackCommand back = new BackCommand();
    GodModeCommand godMode = new GodModeCommand();
    commandManager.register(invsee);
    commandManager.register(back);
    commandManager.register(godMode);
    registerListener(invsee, back, godMode);
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
