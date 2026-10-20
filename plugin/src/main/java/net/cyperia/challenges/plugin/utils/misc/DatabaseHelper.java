package net.cyperia.challenges.plugin.utils.misc;

import com.destroystokyo.paper.profile.ProfileProperty;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DatabaseHelper {

  private static final Map<UUID, String> cachedTextures = new HashMap<>();

  private DatabaseHelper() {
  }

  @Nullable
  public static String getPlayerTextures(@NotNull Player player) {
    return player.getPlayerProfile().getProperties().stream()
      .filter(property -> property.getName().equals("textures"))
      .findFirst()
      .map(ProfileProperty::getValue)
      .orElse(null);
  }

  public static void savePlayerData(@NotNull Player player) {
    try {

      String textures = getPlayerTextures(player);
      if (textures != null) cachedTextures.put(player.getUniqueId(), textures);

      Challenges.getInstance().getDatabaseManager().getDatabase()
        .insertOrUpdate("challenges")
        .set("textures", textures)
        .set("name", player.getName())
        .where("uuid", player.getUniqueId())
        .execute();

    } catch (Exception ex) {
      Logger.error("Unable to update textures for {} | {}", player.getName(), player.getUniqueId(), ex);
    }
  }

  @Nullable
  public static String getTextures(@NotNull UUID uuid) {
    String cached = cachedTextures.get(uuid);
    if (cached != null) return cached;

    Player player = Bukkit.getPlayer(uuid);
    if (player != null) {
      String textures = getPlayerTextures(player);
      if (textures != null) {
        cachedTextures.put(uuid, textures);
        return textures;
      }
    }

    if (!Challenges.getInstance().getDatabaseManager().isConnected())
      return null;

    try {
      String textures = Challenges.getInstance().getDatabaseManager().getDatabase()
        .query("challenges")
        .select("textures")
        .where("uuid", uuid)
        .execute().firstOrEmpty()
        .getString("textures");
      cachedTextures.put(uuid, textures);
      return textures;
    } catch (Exception ex) {
      Logger.error("Unable to get textures for {}", uuid, ex);
      return null;
    }
  }

  public static void clearCache(@NotNull UUID uuid) {
    cachedTextures.remove(uuid);
  }

}
