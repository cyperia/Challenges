package net.cyperia.commons.database;

import net.cyperia.commons.common.config.Propertyable;
import org.jetbrains.annotations.NotNull;

public final class DatabaseConfig {

  private final String host;
  private final String database;
  private final String authDatabase;
  private final String password;
  private final String user;
  private final String file;
  private final int port;
  private final boolean portIsSet;
  private final String replicaSet;

  public DatabaseConfig(String host, String database, String password, String user, int port) {
    this(host, database, null, password, user, port, true, null);
  }

  public DatabaseConfig(String host, String database, String password, String user) {
    this(host, database, null, password, user, 0, false, null);
  }

  public DatabaseConfig(String host, String database, String authDatabase, String password, String user, int port) {
    this(host, database, authDatabase, password, user, port, true, null);
  }

  public DatabaseConfig(String host, String database, String authDatabase, String password, String user) {
    this(host, database, authDatabase, password, user, 0, false, null);
  }

  public DatabaseConfig(String database, String file) {
    this(null, database, null, null, null, 0, false, file);
  }

  public DatabaseConfig(String host, String database, String authDatabase, String password, String user, int port, boolean portIsSet, String file) {
    this(host, database, authDatabase, password, user, port, portIsSet, file, null);
  }

  public DatabaseConfig(String host, String database, String authDatabase, String password, String user, int port, boolean portIsSet, String file, String replicaSet) {
    this.host = host;
    this.database = database;
    this.authDatabase = authDatabase;
    this.password = password;
    this.user = user;
    this.port = port;
    this.portIsSet = portIsSet;
    this.file = file;
    this.replicaSet = replicaSet;
  }

  public DatabaseConfig(@NotNull Propertyable config) {
    this(
      config.getString("host"),
      config.getString("database"),
      config.getString("auth-database"),
      config.getString("password"),
      config.getString("user"),
      config.getInt("port"),
      config.contains("port"),
      config.getString("file"),
      config.getString("replica-set")
    );
  }

  public int getPort() {
    return port;
  }

  public String getAuthDatabase() {
    return authDatabase;
  }

  public String getDatabase() {
    return database;
  }

  public String getHost() {
    return host;
  }

  public String getPassword() {
    return password;
  }

  public String getUser() {
    return user;
  }

  public boolean isPortSet() {
    return portIsSet;
  }

  public String getFile() {
    return file;
  }

  public String getReplicaSet() {
    return replicaSet;
  }

  @Override
  public String toString() {
    return "DatabaseConfig{" +
      "host='" + host + '\'' +
      ", database='" + database + '\'' +
      ", authDatabase='" + authDatabase + '\'' +
      ", user='" + user + '\'' +
      ", file='" + file + '\'' +
      ", port=" + port +
      ", replicaSet='" + replicaSet + '\'' +
      ", portIsSet=" + portIsSet +
      '}';
  }

}
