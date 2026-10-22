package net.cyperia.challenges.plugin.management.challenges;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.management.challenges.entities.GamestateSaveable;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.FileDocument;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.cyperia.commons.common.config.document.wrapper.FileDocumentWrapper;
import net.cyperia.commons.database.exceptions.DatabaseException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public final class ChallengeManager {

  private final List<IChallenge> challenges = new LinkedList<>();
  private final List<GamestateSaveable> additionalSaver = new LinkedList<>();

  private IGoal currentGoal;

  @NotNull
  public List<IChallenge> getChallenges() {
    return Collections.unmodifiableList(challenges);
  }

  public void registerGameStateSaver(@NotNull GamestateSaveable savable) {
    additionalSaver.add(savable);
  }

  public void register(@NotNull IChallenge challenge) {
    challenges.add(challenge);
  }

  public void unregister(@NotNull IChallenge challenge) {
    challenges.remove(challenge);
  }

  public void unregisterIf(@NotNull Predicate<IChallenge> predicate) {
    challenges.removeIf(predicate);
  }

  public void shutdownChallenges() {
    for (IChallenge challenge : challenges) {
      try {
        challenge.handleShutdown();
      } catch (Exception ex) {
        Logger.error("Could not handle shutdown for {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public void clearChallengeCache() {
    challenges.clear();
    currentGoal = null;
  }

  public void restoreDefaults() {
    Logger.debug("Restoring default settings..");
    Challenges.getInstance().getCustomChallengesLoader().resetChallenges();
    for (IChallenge challenge : challenges) {
      try {
        challenge.restoreDefaults();
      } catch (Exception ex) {
        Logger.error("Could not restore defaults for {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public void saveSettings(@NotNull Player player) throws DatabaseException {
    Document document = new GsonDocument();
    saveSettingsInto(document);
    savePlayerDocument(player.getUniqueId(), "config", document);
  }

  public void saveCustomChallenges(@NotNull Player player) throws DatabaseException {
    Document document = new GsonDocument();
    saveCustomChallengesInto(document);
    savePlayerDocument(player.getUniqueId(), "custom_challenges", document);
  }

  public void saveSettings(@NotNull UUID player, @NotNull Document settings) throws DatabaseException {
    savePlayerDocument(player, "config", settings);
  }

  public void saveCustomChallenges(@NotNull UUID player, @NotNull Document customChallenges) throws DatabaseException {
    savePlayerDocument(player, "custom_challenges", customChallenges);
  }

  @NotNull
  public Document getSavedSettings(@NotNull UUID player) throws DatabaseException {
    return getPlayerDocument(player, "config");
  }

  @NotNull
  public Document getSavedCustomChallenges(@NotNull UUID player) throws DatabaseException {
    return getPlayerDocument(player, "custom_challenges");
  }

  private void savePlayerDocument(@NotNull UUID player, @NotNull String field, @NotNull Document document) throws DatabaseException {
    Challenges.getInstance().getDatabaseManager().getDatabase()
      .insertOrUpdate("challenges")
      .where("uuid", player)
      .set(field, document)
      .execute();
  }

  @NotNull
  private Document getPlayerDocument(@NotNull UUID player, @NotNull String field) throws DatabaseException {
    return Challenges.getInstance().getDatabaseManager().getDatabase()
      .query("challenges")
      .select(field)
      .where("uuid", player)
      .execute().firstOrEmpty().getDocument(field);
  }

  public void enable() {
    loadSettings(Challenges.getInstance().getConfigManager().getSettingsConfig().readonly());
    loadCustomChallenges(Challenges.getInstance().getConfigManager().getCustomChallengesConfig().readonly());
    // After the settings: a generator setting creates its worlds on enable, and a location saved in one
    // deserializes to null while that world does not exist
    loadGamestate(Challenges.getInstance().getConfigManager().getGamestateConfig().readonly());
  }

  public synchronized void loadSettings(@NotNull Document config) {

    for (IChallenge challenge : challenges) {
      if (challenge instanceof CustomChallenge) continue;

      String name = savedName(config, challenge.getUniqueName(), challenge.getFallbackNames());
      if (name == null) continue;
      try {
        Document document = config.getDocument(name);
        challenge.loadSettings(document);
      } catch (Exception ex) {
        Logger.error("Could not load setting for challenge {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public synchronized void loadGamestate(@NotNull Document config) {
    // Additional savers first: GameWorldStorage recreates the void world, which challenge locations may point into
    LinkedList<GamestateSaveable> list = new LinkedList<>(additionalSaver);
    list.addAll(challenges);
    for (GamestateSaveable challenge : list) {
      String name = savedName(config, challenge.getUniqueGamestateName(), challenge.getFallbackNames());
      if (name == null) continue;
      try {
        Document document = config.getDocument(name);
        challenge.loadGameState(document);

        if (challenge instanceof AbstractChallenge) {
          AbstractChallenge abstractChallenge = (AbstractChallenge) challenge;
          if (abstractChallenge.getBossbar().isShown()) {
            abstractChallenge.getBossbar().update();
          }
          if (abstractChallenge.getScoreboard().isShown()) {
            abstractChallenge.getScoreboard().update();
          }
        }

      } catch (Exception ex) {
        Logger.error("Could not load gamestate for {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public synchronized void loadCustomChallenges(@NotNull Document config) {
    Challenges.getInstance().getCustomChallengesLoader().loadCustomChallengesFrom(config);
  }

  public void resetGamestate() {
    LinkedList<GamestateSaveable> list = new LinkedList<>(challenges);
    list.addAll(additionalSaver);
    for (GamestateSaveable challenge : list) {
      try {
        challenge.loadGameState(Document.empty());

        if (challenge instanceof AbstractChallenge abstractChallenge) {
          if (abstractChallenge.getBossbar().isShown()) {
            abstractChallenge.getBossbar().update();
          }
          if (abstractChallenge.getScoreboard().isShown()) {
            abstractChallenge.getScoreboard().update();
          }
        }
      } catch (Exception ex) {
        Logger.error("Could not load gamestate for {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public void saveGameStateInto(@NotNull Document config) {
    List<GamestateSaveable> list = new ArrayList<>(challenges);
    list.addAll(additionalSaver);
    for (GamestateSaveable challenge : list) {
      try {
        Document document = config.getDocument(challenge.getUniqueGamestateName());
        challenge.writeGameState(document);
        challenge.getFallbackNames().forEach(config::remove);
      } catch (Exception ex) {
        Logger.error("Could not write gamestate of {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public synchronized void saveGamestate(boolean async) {
    FileDocument config = Challenges.getInstance().getConfigManager().getGamestateConfig();
    saveGameStateInto(config);
    config.save(async);
  }

  public void saveSettingsInto(@NotNull Document config) {
    for (IChallenge challenge : challenges) {
      if (challenge instanceof CustomChallenge) continue;
      try {
        Document document = config.getDocument(challenge.getUniqueGamestateName());
        challenge.writeSettings(document);
        challenge.getFallbackNames().forEach(config::remove);
      } catch (Exception ex) {
        Logger.error("Could not write settings of {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public synchronized void saveLocalSettings(boolean async) {
    FileDocument config = Challenges.getInstance().getConfigManager().getSettingsConfig();
    saveSettingsInto(config);
    config.save(async);
  }

  public void saveCustomChallengesInto(@NotNull Document config) {
    Collection<CustomChallenge> customChallenges = Challenges.getInstance().getCustomChallengesLoader()
      .getCustomChallenges().values();

    for (IChallenge challenge : customChallenges) {
      try {
        Document document = config.getDocument(challenge.getUniqueName());
        challenge.writeSettings(document);
        challenge.writeGameState(document);
      } catch (Exception ex) {
        Logger.error("Could not write settings of {}", challenge.getClass().getSimpleName(), ex);
      }
    }
  }

  public synchronized void saveLocalCustomChallenges(boolean async) {
    FileDocument config = new FileDocumentWrapper(Challenges.getInstance().getDataFile("internal/custom_challenges.json"), new GsonDocument());
    saveCustomChallengesInto(config);
    config.save(async);
  }


  // The key something was saved under in config: its name, else an id it had before
  @Nullable
  static String savedName(@NotNull Document config, @NotNull String name, @NotNull List<String> fallbackIds) {
    if (config.contains(name)) return name;
    return fallbackIds.stream().filter(config::contains).findFirst().orElse(null);
  }

  @Nullable
  public IGoal getCurrentGoal() {
    return currentGoal;
  }

  public void setCurrentGoal(@Nullable IGoal goal) {

    IGoal oldGoal = currentGoal;
    currentGoal = goal;

    if (oldGoal != null)
      oldGoal.setEnabled(false);

    if (goal != null)
      goal.setEnabled(true);

  }

}
