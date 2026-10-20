package net.cyperia.challenges.plugin.management.server;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.challenges.entities.GamestateSaveable;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Manages the portal locations of custom worlds
 */
public class GeneratorWorldPortalManager implements GamestateSaveable {

  public Map<UUID, Location> lastWorldLocations;

  public GeneratorWorldPortalManager() {
    lastWorldLocations = new HashMap<>();
    Challenges.getInstance().getChallengeManager().registerGameStateSaver(this);
  }

  @Nullable
  public Location getAndRemoveLastWorld(@NotNull Player player) {
    return lastWorldLocations.remove(player.getUniqueId());
  }

  public void setLastLocation(@NotNull Player player, @NotNull Location location) {
    lastWorldLocations.put(player.getUniqueId(), location);
  }

  public boolean isCustomWorld(@NotNull String name) {
    return Challenges.getInstance().getGameWorldStorage().getCustomGeneratedGameWorlds().contains(name);
  }

  @Override
  public String getUniqueGamestateName() {
    return getClass().getSimpleName().toLowerCase();
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    for (Entry<UUID, Location> entry : lastWorldLocations.entrySet()) {
      UUID uuid = entry.getKey();
      Location location = entry.getValue();
      document.set(uuid.toString(), location);
    }
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    for (String key : document.keys()) {
      try {
        Location location = document.getInstance(key, Location.class);
        UUID uuid = UUID.fromString(key);
        lastWorldLocations.put(uuid, location);
      } catch (Exception exception) {
        Challenges.getInstance().getILogger().error("Couldn't load last location of: " + key);
        Challenges.getInstance().getILogger().error("", exception);
      }
    }
  }

}
