package net.cyperia.commons.bukkit.core;

import net.cyperia.commons.bukkit.utils.chat.ChatInputListener;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import net.cyperia.commons.bukkit.utils.menu.MenuPositionListener;
import net.cyperia.commons.bukkit.utils.misc.MinecraftVersion;
import net.cyperia.commons.bukkit.utils.wrapper.ActionListener;
import net.cyperia.commons.bukkit.utils.wrapper.SimpleEventExecutor;
import net.cyperia.commons.common.annotations.ReplaceWith;
import net.cyperia.commons.common.collection.NamedThreadFactory;
import net.cyperia.commons.common.collection.WrappedException;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.FileDocument;
import net.cyperia.commons.common.config.document.YamlDocument;
import net.cyperia.commons.common.config.document.YamlHelper;
import net.cyperia.commons.common.logging.ILogger;
import net.cyperia.commons.common.logging.internal.BukkitLoggerWrapper;
import net.cyperia.commons.common.logging.lib.JavaILogger;
import net.cyperia.commons.common.version.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;

public abstract class BukkitModule extends JavaPlugin {

  private static volatile BukkitModule firstInstance;
  private static boolean setFirstInstance = true;
  private static boolean wasShutdown;

  private final Map<String, CommandExecutor> commandsQueue = new HashMap<>();
  private final List<Listener> listenersQueue = new ArrayList<>();
  private final SimpleConfigManager configManager = new SimpleConfigManager(this);

  private JavaILogger logger;
  private ExecutorService executorService;
  private Document config, pluginConfig, defaultConfig;
  private Version version;
  private boolean firstInstall;
  private boolean isReloaded;
  private boolean isLoaded;

  private boolean requirementsMet = true;
  private boolean versionSupported = true;

  @Override
  public final void onLoad() {
    isLoaded = true;

    RequirementsChecker requirements = new RequirementsChecker(this);
    Document required = getPluginDocument().getDocument("require");
    requirementsMet = requirements.check(required);
    if (!requirementsMet)
      return;
    versionSupported = requirements.checkVersion(required);

    if (setFirstInstance || firstInstance == null) {
      setFirstInstance(this);
    }

    ILogger.setConstantFactory(this.getILogger());
    if (wasShutdown) isReloaded = true;
    firstInstall = !getDataFolder().exists();
    if (firstInstall) {
      getILogger().info("Detected first install!");
    }
    trySaveDefaultConfig();
    getILogger().setLevel(Level.INFO);

    injectInstance();

    try {
      handleLoad();
    } catch (Exception ex) {
      throw new WrappedException(ex);
    }
  }

  @Override
  public final void onEnable() {
    if (!requirementsMet) {
      RequirementsChecker requirements = new RequirementsChecker(this);
      getServer().getPluginManager().registerEvents(new Listener() {
        @EventHandler
        public void onJoin(@NotNull PlayerJoinEvent event) {
          requirements.sendPaperGuide(event.getPlayer());
        }
      }, this);
      return;
    }
    if (!versionSupported) {
      // Paper runs the tasks scheduled during startup right before "Done", so the warning is repeated at the end of the startup log
      getServer().getScheduler().runTask(this, () -> new RequirementsChecker(this).checkVersion(getPluginDocument().getDocument("require")));
    }

    commandsQueue.forEach((name, executor) -> registerCommand0(executor, name));
    commandsQueue.clear();

    listenersQueue.forEach(this::registerListener);
    listenersQueue.clear();


    try {
      handleEnable();
    } catch (Exception ex) {
      throw new WrappedException(ex);
    }
  }

  @Override
  public final void onDisable() {
    Throwable error = null;
    try {
      handleDisable();
    } catch (Throwable ex) {
      error = ex;
    }

    setFirstInstance = true;
    wasShutdown = true;
    isLoaded = false;

    if (executorService != null)
      executorService.shutdown();

    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.getOpenInventory().getTopInventory().getHolder() == MenuPosition.HOLDER)
        player.getOpenInventory().close();
    }

    if (error != null)
      throw new WrappedException(error);
  }

  protected void handleLoad() throws Exception {
  }

  protected void handleEnable() throws Exception {
  }

  protected void handleDisable() throws Exception {
  }

  public final boolean isVersionSupported() {
    return versionSupported;
  }

  public final boolean isFirstInstall() {
    return firstInstall;
  }

  public final boolean isReloaded() {
    return isReloaded;
  }

  public final boolean isLoaded() {
    return isLoaded;
  }

  public final boolean isFirstInstance() {
    return firstInstance == this;
  }

  @NotNull
  public JavaILogger getILogger() {
    return logger != null ? logger : (logger = new BukkitLoggerWrapper(super.getLogger()));
  }

  @NotNull
  public Document getConfigDocument() {
    checkLoaded();
    return config != null ? config : (config = new YamlDocument(super.getConfig()));
  }

  @NotNull
  public File getConfigFile() {
    return getDataFile("config.yml");
  }

  @Override
  public void reloadConfig() {
    config = null;
    super.reloadConfig();
  }

  /**
   * @return the plugin configuration (plugin.yml) as document
   */
  @NotNull
  public Document getPluginDocument() {
    if (pluginConfig != null) return pluginConfig;
    InputStream resource = getResource("plugin.yml");
    if (resource == null) throw new IllegalStateException("Could not load plugin.yml as resource");
    return pluginConfig = new YamlDocument(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8)));
  }

  /**
   * This is the configuration that is shipped with the plugin and can be used to check for default values.
   *
   * @return the default configuration (config.yml) as document or {@code null} if it could not be read
   */
  @Nullable
  public Document getDefaultConfigDocument() {
    if (defaultConfig != null) return defaultConfig;
    try {
      InputStream resource = getResource("config.yml");
      if (resource == null) return null;
      return defaultConfig = new YamlDocument(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      getILogger().error("Could not load default config.yml as resource", ex);
      return null;
    }
  }

  @NotNull
  public FileDocument getConfig(@NotNull String filename) {
    return configManager.getDocument(filename);
  }

  @NotNull
  public Version getVersion() {
    return version != null ? version : (version = Version.parse(getDescription().getVersion()));
  }

  @NotNull
  @Override
  @Deprecated
  @ReplaceWith("getConfigDocument()")
  public FileConfiguration getConfig() {
    return super.getConfig();
  }

  @Override
  @Deprecated
  public void saveConfig() {
    super.saveConfig();
  }

  public final <T extends CommandExecutor & Listener> void registerListenerCommand(@NotNull T listenerAndExecutor, @NotNull String... names) {
    registerCommand(listenerAndExecutor, names);
    registerListener(listenerAndExecutor);
  }

  public final void registerCommand(@NotNull CommandExecutor executor, @NotNull String... names) {
    for (String name : names) {
      if (isEnabled()) {
        registerCommand0(executor, name);
      } else {
        commandsQueue.put(name, executor);
      }
    }
  }

  private void registerCommand0(@NotNull CommandExecutor executor, @NotNull String name) {
    PluginCommand command = getCommand(name);
    if (command == null) {
      getILogger().warn("Tried to register invalid command '{}'", name);
    } else {
      command.setExecutor(executor);
    }
  }

  public final void registerListener(@NotNull Listener... listeners) {
    if (isEnabled()) {
      for (Listener listener : listeners) {
        registerListener0(listener);
      }
    } else {
      this.listenersQueue.addAll(Arrays.asList(listeners));
    }
  }

  private void registerListener0(@NotNull Listener listener) {
    if (listener instanceof ActionListener<?> actionListener) {
      getServer().getPluginManager().registerEvent(
        actionListener.getClassOfEvent(), actionListener, actionListener.getPriority(),
        new SimpleEventExecutor(actionListener.getClassOfEvent(), actionListener.getListener()), this, actionListener.isIgnoreCancelled()
      );
    } else {
      getServer().getPluginManager().registerEvents(listener, this);
    }
  }

  public final <E extends Event> void on(@NotNull Class<E> classOfEvent, @NotNull Consumer<? super E> action) {
    on(classOfEvent, EventPriority.NORMAL, action);
  }

  public final <E extends Event> void on(@NotNull Class<E> classOfEvent, @NotNull EventPriority priority, @NotNull Consumer<? super E> action) {
    on(classOfEvent, priority, false, action);
  }

  public final <E extends Event> void on(@NotNull Class<E> classOfEvent, @NotNull EventPriority priority, boolean ignoreCancelled, @NotNull Consumer<? super E> action) {
    registerListener(new ActionListener<>(classOfEvent, action, priority, ignoreCancelled));
  }

  public final void disablePlugin() {
    getServer().getPluginManager().disablePlugin(this);
  }

  @NotNull
  public final File getDataFile(@NotNull String filename) {
    return new File(getDataFolder(), filename);
  }

  @NotNull
  public final File getDataFile(@NotNull String subfolder, @NotNull String filename) {
    return new File(getDataFile(subfolder), filename);
  }

  @NotNull
  public ExecutorService getExecutor() {
    if (executorService != null) return executorService;
    ThreadFactory factory = new NamedThreadFactory(threadId -> String.format("%s-Task-%s", this.getName(), threadId));
    return executorService = Executors.newCachedThreadPool(factory);
  }

  public void runAsync(@NotNull Runnable task) {
    getExecutor().submit(task);
  }

  public final void checkLoaded() {
    if (!isLoaded())
      throw new IllegalStateException("Plugin (" + getName() + ") is not loaded yet");
  }

  public final void checkEnabled() {
    if (!isEnabled())
      throw new IllegalStateException("Plugin (" + getName() + ") is not enabled yet");
  }

  private void registerAsFirstInstance() {
    getILogger().info(getName() + " was loaded as the first BukkitModule");
    registerListener(
      new MenuPositionListener(),
      new ChatInputListener()
    );
    getILogger().info("Detected server version {}", MinecraftVersion.current());
  }

  private void trySaveDefaultConfig() {
    try {
      saveDefaultConfig();
    } catch (IllegalArgumentException ex) {
      // No default config exists
    }
  }


  /**
   * Replaces the value of an <b>already existing</b> key inside the {@code config.yml} while leaving every
   * comment (and the rest of the file's formatting) untouched.
   * Bukkit's {@link #saveConfig()} re-serializes the whole document and therefore strips all comments.
   *
   * @param key   the dot-separated path of an existing config value
   * @param value the new value, or {@code null} to write a literal {@code null}
   * @return whether the value could be set
   */
  public boolean setValueInConfig(@NotNull String key, @Nullable Object value) {
    File file = getConfigFile();
    if (!file.exists()) {
      getILogger().warn("Cannot replace '{}' in config.yml: file does not exist", key);
      return false;
    }

    try {
      String content = Files.readString(file.toPath());
      String updated = YamlHelper.replaceValue(content, key, value);
      if (updated == null) {
        getILogger().warn("Cannot replace '{}' in config.yml: key not found or has a multi-line value", key);
        return false;
      }

      Files.writeString(file.toPath(), updated);

      // keep the loaded configuration in sync with the file we just edited
      if (isLoaded()) getConfigDocument().set(key, value);
      return true;
    } catch (IOException ex) {
      getILogger().error("Could not replace '{}' in config.yml: {}", key, ex.getMessage());
      return false;
    }
  }

  private void injectInstance() {
    try {
      Field instanceField = this.getClass().getDeclaredField("instance");
      instanceField.setAccessible(true);
      instanceField.set(null, this);
    } catch (Throwable ex) {
    }
  }

  @NotNull
  public static BukkitModule getFirstInstance() {
    if (firstInstance == null) {
      JavaPlugin provider = JavaPlugin.getProvidingPlugin(BukkitModule.class);
      if (!(provider instanceof BukkitModule))
        throw new IllegalStateException("No BukkitModule was initialized yet & BukkitModule class was not loaded by a BukkitModule");
      firstInstance = (BukkitModule) provider;
    }

    return firstInstance;
  }

  private static synchronized void setFirstInstance(@NotNull BukkitModule module) {
    setFirstInstance = false;
    firstInstance = module;
    module.registerAsFirstInstance();
  }

  @NotNull
  public static BukkitModule getProvidingModule(@NotNull Class<?> clazz) {
    JavaPlugin provider = JavaPlugin.getProvidingPlugin(clazz);
    if (!(provider instanceof BukkitModule))
      throw new IllegalStateException(clazz.getName() + " is not provided by a BukkitModule");
    return (BukkitModule) provider;
  }

}
