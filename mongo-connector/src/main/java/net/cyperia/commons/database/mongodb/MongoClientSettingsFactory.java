package net.cyperia.commons.database.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterConnectionMode;
import java.util.Collections;
import net.cyperia.commons.database.DatabaseConfig;
import org.jetbrains.annotations.NotNull;

public final class MongoClientSettingsFactory {

  private MongoClientSettingsFactory() {
  }

  @NotNull
  public static MongoClientSettings create(@NotNull DatabaseConfig config) {
    MongoCredential credential = MongoCredential.createCredential(config.getUser(), config.getAuthDatabase(), config.getPassword().toCharArray());
    ServerAddress seed = new ServerAddress(config.getHost(), config.isPortSet() ? config.getPort() : ServerAddress.defaultPort());
    String replicaSet = config.getReplicaSet();
    return MongoClientSettings.builder()
      .retryReads(false)
      .credential(credential)
      .applyToClusterSettings(builder -> {
        builder.hosts(Collections.singletonList(seed));
        if (replicaSet != null && !replicaSet.isBlank()) {
          builder.mode(ClusterConnectionMode.MULTIPLE);
          builder.requiredReplicaSetName(replicaSet);
        }
      })
      .build();
  }
}
