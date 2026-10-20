package net.cyperia.challenges.plugin.management.database;

import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.utils.logging.ConsolePrint;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.collection.pair.Tuple;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.database.Database;
import net.cyperia.commons.database.DatabaseConfig;
import net.cyperia.commons.database.SQLColumn;
import net.cyperia.commons.database.action.ExecutedQuery;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.sql.abstraction.AbstractSQLDatabase;
import net.cyperia.commons.database.sql.mysql.MySQLDatabase;
import net.cyperia.commons.database.sql.sqlite.SQLiteDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public final class DatabaseManager {

  private final Map<String, Tuple<String, JavaPlugin>> registry = new HashMap<>();
  @Getter
  private String type;
  @Getter
  private Database database;

  {
    // Database types supported by default
    registerDatabase("sqlite", SQLiteDatabase.class, Challenges.getInstance());
    registerDatabase("mysql", MySQLDatabase.class, Challenges.getInstance());
  }

  public void enable() {
    Document config = Challenges.getInstance().getConfigDocument();

    type = config.getString(ConfigManager.Keys.DATABASE_TYPE, "none").toLowerCase();
    if ("none".equals(type)) return;

    // Check dependencies
    if ("mongodb".equals(type) && !checkDependencies("com.mongodb.client.MongoClient")) {
      ConsolePrint.noMongoDependencies();
      return;
    }

    try {
      Tuple<String, JavaPlugin> pair = getDatabaseForName(type);
      if (pair == null) {
        Logger.error("Selected illegal database type '{}'", type);
        return;
      }

      JavaPlugin provider = pair.getSecond();
      PluginManager manager = Bukkit.getPluginManager();
      if (!manager.isPluginEnabled(provider) && provider != Challenges.getInstance()) {
        manager.enablePlugin(provider);
      }

      ClassLoader loader = provider.getClass().getClassLoader();

      String className = pair.getFirst();
      @SuppressWarnings("unchecked")
      Class<? extends Database> classOfDatabase = (Class<? extends Database>) loader.loadClass(className);

      DatabaseConfig databaseConfig = new DatabaseConfig(config.getDocument(ConfigManager.Keys.DATABASE_SECTION).getDocument(type));
      Constructor<? extends Database> constructor = classOfDatabase.getDeclaredConstructor(DatabaseConfig.class);
      database = constructor.newInstance(databaseConfig);

      connect();
    } catch (ClassNotFoundException ex) {
      Logger.error("Could not find class for database '{}'", type, ex);
    } catch (Throwable ex) {
      Logger.error("Could not create database", ex);
    }
  }

  private void connect() {
    Challenges.getInstance().runAsync(() -> {
      database.connectSafely();
      database.createTableSafely("challenges",
        new SQLColumn("uuid", "varchar", 36),
        new SQLColumn("name", "varchar", 16),
        new SQLColumn("textures", "varchar", 500),
        new SQLColumn("config", "varchar", 15000),
        new SQLColumn("custom_challenges", "text", 30000)
      );
      loadMigration();
    });
  }

  private void loadMigration() {

    if (database instanceof AbstractSQLDatabase) {
      AbstractSQLDatabase sqlDatabase = (AbstractSQLDatabase) this.database;

      // Create custom_challenges column
      try {
        ExecutedQuery execute = sqlDatabase.query("challenges").select("custom_challenges").execute();
      } catch (DatabaseException databaseException) {
        try {
          sqlDatabase.prepare("ALTER TABLE `challenges` ADD COLUMN `custom_challenges` text(60000)").execute();
          Challenges.getInstance().getILogger().info("Creating not existing column 'custom_challenges' in SQL Database");
        } catch (Exception exception) {
          Challenges.getInstance().getILogger().error("Failed to create non existing column 'custom_challenges' in SQL Database");
          Challenges.getInstance().getILogger().error("", exception);
        }
      }

    }

  }

  public void disconnectIfConnected() {
    if (database != null) {
      database.disconnectSafely();
    }
  }

  @Nullable
  private Tuple<String, JavaPlugin> getDatabaseForName(@NotNull String type) throws ClassNotFoundException {
    return registry.get(type);
  }

  public void registerDatabase(@NotNull String name, @NotNull Class<? extends Database> classOfDatabase, @NotNull JavaPlugin provider) {
    registry.put(name, new Tuple<>(classOfDatabase.getName(), provider));
  }

  public boolean isConnected() {
    return database != null && database.isConnected();
  }

  public boolean isEnabled() {
    return database != null;
  }

  private boolean checkDependencies(@NotNull String... classes) {
    try {
      for (String name : classes) {
        Class.forName(name);
      }
    } catch (Throwable ex) {
      return false;
    }
    return true;
  }

}
