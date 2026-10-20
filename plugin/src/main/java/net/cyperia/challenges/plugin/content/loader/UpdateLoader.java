package net.cyperia.challenges.plugin.content.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.version.Version;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class UpdateLoader extends ContentLoader {

  public static final String DOWNLOAD = "modrinth.com/plugin/challengesplugin";

  @Getter
  private static boolean newestPluginVersion = true;
  @Getter
  private static boolean newestConfigVersion = true;
  @Getter
  private static Version defaultConfigVersion;
  @Getter
  private static Version currentConfigVersion;

  @Override
  protected void load() {
    try {
      Version pluginVersion = Challenges.getInstance().getVersion();
      Document defaultConfig = Challenges.getInstance().getDefaultConfigDocument();
      defaultConfigVersion = defaultConfig == null ? pluginVersion : Version.parse(defaultConfig.getString(ConfigManager.Keys.CONFIG_VERSION));
      currentConfigVersion = Version.parse(Challenges.getInstance().getConfigDocument().getString(ConfigManager.Keys.CONFIG_VERSION));
      Version latestVersion = newestRelease(fetchVersions(Bukkit.getMinecraftVersion()));

      if (latestVersion != null && latestVersion.isNewerThan(pluginVersion)) {
        Logger.info("A new version of Challenges is available: {}, you have {}", latestVersion, pluginVersion);
        newestPluginVersion = false;
      }
      if (defaultConfigVersion.isNewerThan(currentConfigVersion)) {
        Logger.info("A new version of the config (plugins/Challenges/config.yml) is available");
        newestConfigVersion = false;
      }

      callSubscribers();
    } catch (Exception ex) {
      Logger.error("Could not check for update: {}", ex.getMessage());
    }
  }

  // Only versions made for this server's Minecraft version count, since each release runs on one version only
  @NotNull
  private static String fetchVersions(@NotNull String minecraftVersion) throws Exception {
    String query = "loaders=" + URLEncoder.encode("[\"paper\"]", StandardCharsets.UTF_8)
      + "&game_versions=" + URLEncoder.encode("[\"" + minecraftVersion + "\"]", StandardCharsets.UTF_8);
    HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.modrinth.com/v2/project/challengesplugin/version?" + query))
      .header("User-Agent", "cyperia/Challenges/" + Challenges.getInstance().getVersion().format() + " (cyperia.net)")
      .timeout(Duration.ofSeconds(10))
      .build();
    try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) throw new IllegalStateException("Modrinth answered " + response.statusCode());
      return response.body();
    }
  }

  @Nullable
  static Version newestRelease(@NotNull String versionsJson) {
    Version newest = null;
    for (JsonElement element : JsonParser.parseString(versionsJson).getAsJsonArray()) {
      JsonObject version = element.getAsJsonObject();
      if (!"release".equals(version.get("version_type").getAsString())) continue;
      Version number = Version.parse(version.get("version_number").getAsString());
      if (newest == null || number.isNewerThan(newest)) newest = number;
    }
    return newest;
  }

}
