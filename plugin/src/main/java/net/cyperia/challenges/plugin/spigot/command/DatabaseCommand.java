package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.challenges.ChallengeManager;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DatabaseCommand implements ChallengeCommand {

  private final Map<String, DatabaseCommandExecutor> databaseExecutors;
  private final boolean savePlayerConfigs;
  private final boolean savePlayerChallenges;

  public DatabaseCommand() {
    savePlayerConfigs = Challenges.getInstance().getConfigDocument().getBoolean(ConfigManager.Keys.SAVE_PLAYER_CONFIGS);
    savePlayerChallenges = Challenges.getInstance().getConfigDocument().getBoolean(ConfigManager.Keys.SAVE_PLAYER_CUSTOM_CHALLENGES);

    databaseExecutors = Map.of("settings", new DatabaseCommandExecutor() {
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
    }, "customs", new DatabaseCommandExecutor() {
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

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("database", ChallengesPermissions.COMMAND_DATABASE).withAliases("db");
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.then(action(nodes, "save", ChallengesPermissions.COMMAND_DATABASE_SAVE, DatabaseCommandExecutor::save));
    root.then(action(nodes, "load", ChallengesPermissions.COMMAND_DATABASE_LOAD, DatabaseCommandExecutor::load));
    root.then(action(nodes, "reset", ChallengesPermissions.COMMAND_DATABASE_RESET, DatabaseCommandExecutor::reset));
  }

  @NotNull
  private <S> LiteralArgumentBuilder<S> action(@NotNull CommandNodes<S> nodes, @NotNull String name,
                                               @NotNull String permission, @NotNull DatabaseAction action) {
    LiteralArgumentBuilder<S> node = nodes.literal(name).requires(nodes.permission(permission));
    databaseExecutors.forEach((type, executor) -> node.then(nodes.literal(type).executes(nodes.runAsPlayer((player, context) -> {
      if (checkFeatureDisabled(Challenges.getInstance().getDatabaseManager().isConnected(), player)) return;
      action.run(executor, player);
    }))));
    return node;
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

  private interface DatabaseCommandExecutor {
    void save(Player player) throws Exception;

    void load(Player player) throws Exception;

    void reset(Player player) throws Exception;
  }

  private interface DatabaseAction {
    void run(DatabaseCommandExecutor executor, Player player) throws Exception;
  }

}
