package net.cyperia.challenges.plugin.spigot.command;

import com.google.common.collect.Lists;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.challenges.ChallengeManager;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseCommand implements PlayerCommand, TabCompleter {

  private final Map<String, DatabaseCommandExecutor> databaseExecutors;
  private final boolean savePlayerConfigs;
  private final boolean savePlayerChallenges;

  public DatabaseCommand() {
    savePlayerConfigs = Challenges.getInstance().getConfigDocument().getBoolean(ConfigManager.Keys.SAVE_PLAYER_CONFIGS);
    savePlayerChallenges = Challenges.getInstance().getConfigDocument().getBoolean(ConfigManager.Keys.SAVE_PLAYER_CUSTOM_CHALLENGES);

    databaseExecutors = new HashMap<>();
    databaseExecutors.put("settings", new DatabaseCommandExecutor() {
      @Override
      public void save(Player player) throws Exception {
        if (checkFeatureDisabled(savePlayerConfigs, player)) return;
        Challenges.getInstance().getChallengeManager().saveSettings(player);
        MessageKey.of("command.database.config-saved").send(player, Prefix.CHALLENGES);
      }

      @Override
      public void load(Player player) throws Exception {
        if (checkFeatureDisabled(savePlayerConfigs, player)) return;
        ChallengeManager manager = Challenges.getInstance().getChallengeManager();
        manager.loadSettings(manager.getSavedSettings(player.getUniqueId()));
        MessageKey.of("command.database.config-loaded").send(player, Prefix.CHALLENGES);
      }

      @Override
      public void reset(Player player) throws Exception {
        if (checkFeatureDisabled(savePlayerConfigs, player)) return;
        Challenges.getInstance().getDatabaseManager().getDatabase().update("challenges")
          .where("uuid", player.getUniqueId())
          .set("config", null)
          .execute();
        MessageKey.of("command.database.config-reset").send(player, Prefix.CHALLENGES);
      }
    });
    databaseExecutors.put("customs", new DatabaseCommandExecutor() {

      @Override
      public void save(Player player) throws Exception {
        if (checkFeatureDisabled(savePlayerChallenges, player)) return;
        Challenges.getInstance().getChallengeManager().saveCustomChallenges(player);
        MessageKey.of("command.database.customs-saved").send(player, Prefix.CHALLENGES);
      }

      @Override
      public void load(Player player) throws Exception {
        if (checkFeatureDisabled(savePlayerChallenges, player)) return;
        ChallengeManager manager = Challenges.getInstance().getChallengeManager();
        manager.loadCustomChallenges(manager.getSavedCustomChallenges(player.getUniqueId()));
        MessageKey.of("command.database.customs-loaded").send(player, Prefix.CHALLENGES);
      }

      @Override
      public void reset(Player player) throws Exception {
        if (checkFeatureDisabled(savePlayerChallenges, player)) return;
        Challenges.getInstance().getDatabaseManager().getDatabase().update("challenges")
          .where("uuid", player.getUniqueId())
          .set("custom_challenges", null)
          .execute();
        MessageKey.of("command.database.customs-reset").send(player, Prefix.CHALLENGES);
      }
    });
  }

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {

    if (checkFeatureDisabled(Challenges.getInstance().getDatabaseManager().isConnected(), player)) {
      return;
    }

    if (args.length != 2 || !databaseExecutors.containsKey(args[1])) {
      MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "database <save/load/reset> <settings/customs>");
      return;
    }

    String type = args[1].toLowerCase();
    DatabaseCommandExecutor executor = databaseExecutors.get(type);

    switch (args[0].toLowerCase()) {
      case "save":
        executor.save(player);
        break;
      case "load":
        executor.load(player);
        break;
      case "reset":
        executor.reset(player);
        break;
      default:
        MessageKey.of("command.syntax").send(player, Prefix.CHALLENGES, "database <save/load/reset> <settings/customs>");
    }

  }

  /**
   * Checks for disabled features and sends a proper messages to indicate that
   */
  private boolean checkFeatureDisabled(boolean enabled, @NotNull Player player) {
    if (!enabled) {
      MessageKey.of("command.feature-disabled").send(player, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.play(player);
      return true;
    }
    return false;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) {

    if (args.length <= 1) {
      return Utils.filterRecommendations(args[0], "save", "load", "reset");
    } else if (args.length == 2) {
      return Utils.filterRecommendations(args[1], databaseExecutors.keySet().toArray(new String[0]));
    }
    return Lists.newLinkedList();
  }


  private interface DatabaseCommandExecutor {
    void save(Player player) throws Exception;

    void load(Player player) throws Exception;

    void reset(Player player) throws Exception;
  }

}
