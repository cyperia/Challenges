package net.cyperia.challenges.mongoconnector;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.database.mongodb.MongoDBDatabase;
import org.bukkit.plugin.java.JavaPlugin;


public final class MongoConnector extends JavaPlugin {

  @Override
  public void onLoad() {
    Challenges.getInstance().getDatabaseManager().registerDatabase("mongodb", MongoDBDatabase.class, this);
  }

  @Override
  public void onDisable() {
    // This plugin depends on Challenges, so it is disabled first and its classloader (which owns the
    // shaded MongoDB driver) is closed before Challenges#handleDisable runs. Closing the client here,
    // while the classloader is still alive, avoids a "zip file error" when the driver lazily loads
    // shutdown classes. Challenges' own disconnectIfConnected then becomes a no-op.
    Challenges challenges = Challenges.getInstance();
    if (challenges != null && challenges.getDatabaseManager() != null) {
      challenges.getDatabaseManager().disconnectIfConnected();
    }
  }

}
