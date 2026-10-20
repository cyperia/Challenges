package net.cyperia.challenges.plugin.management.challenges;

import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.custom.TriggerActionChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.IChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.generator.types.IChallengesMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.IMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.CustomHomeMenuGenerator;
import net.cyperia.challenges.plugin.utils.misc.MapUtils;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class CustomChallengesLoader extends ModuleChallengeLoader {

  private final Map<UUID, CustomChallenge> customChallenges = new LinkedHashMap<>();

  private final int maxNameLength;

  public CustomChallengesLoader() {
    super(Challenges.getInstance());
    maxNameLength = Challenges.getInstance().getConfigDocument().getInt(ConfigManager.Keys.CUSTOM_CHALLENGE_MAX_NAME_LENGTH);
  }

  public CustomChallenge registerCustomChallenge(@NotNull UUID uuid, Material material, String name, ChallengeTrigger trigger,
                                                 Map<String, String[]> subTriggers, ChallengeAction action, Map<String, String[]> subActions, boolean generate) {
    CustomChallenge challenge = customChallenges.getOrDefault(uuid, new CustomChallenge(uuid, material, name, trigger, subTriggers, action, subActions));
    if (!customChallenges.containsKey(uuid)) {
      customChallenges.put(uuid, challenge);
      register(challenge);
    } else {
      challenge.applySettings(material, name, trigger, subTriggers, action, subActions);
    }
    generateCustomChallenge(challenge, false, generate);
    return challenge;
  }

  public void unregisterCustomChallenge(@NotNull UUID uuid) {
    CustomChallenge challenge = customChallenges.remove(uuid);
    if (challenge == null) return;
    Challenges.getInstance().getChallengeLoader().unregister(challenge);
    generateCustomChallenge(challenge, true, true);
  }

  public void loadCustomChallengesFrom(@NotNull Document document) {
    customChallenges.clear();
    Challenges.getInstance().getChallengeManager().unregisterIf(iChallenge -> iChallenge.getMenu() == MenuType.CUSTOM);
    MenuType.CUSTOM.getMenuGenerator().resetCache();

    for (String key : document.keys()) {
      try {
        Document doc = document.getDocument(key);

        UUID uuid = UUID.fromString(key);
        String name = doc.getString("name");
        Material material = doc.getEnum("material", Material.class);
        ChallengeTrigger trigger = Challenges.getInstance().getCustomSettingsLoader().getTriggerByName(doc.getString("trigger"));
        Map<String, String[]> subTriggers = MapUtils.createSubSettingsMapFromDocument(doc.getDocument("subTrigger"));
        ChallengeAction action = Challenges.getInstance().getCustomSettingsLoader().getActionByName(doc.getString("action"));
        Map<String, String[]> subActions = MapUtils.createSubSettingsMapFromDocument(doc.getDocument("subActions"));

        CustomChallenge challenge = registerCustomChallenge(uuid, material, name, trigger, subTriggers, action, subActions, false);
        challenge.setEnabled(doc.getBoolean("enabled"));

      } catch (Exception exception) {
        Challenges.getInstance().getILogger().error("Something went wrong while initializing custom challenge {} :: {}", key, exception.getMessage());
        Challenges.getInstance().getILogger().error("", exception);
      }

    }

    MenuType.CUSTOM.getMenuGenerator().getListGenerator().updatePages();
  }

  public void resetChallenges() {
    customChallenges.clear();
    Challenges.getInstance().getChallengeManager().unregisterIf(iChallenge -> iChallenge.getMenu() == MenuType.CUSTOM);
    MenuType.CUSTOM.getMenuGenerator().resetCache();
  }

  private void generateCustomChallenge(CustomChallenge challenge, boolean deleted, boolean generate) {
    IChallengesMenuGenerator generator = challenge.getMenu().getMenuGenerator();

    if (deleted) {
      generator.removeFromCache(challenge);
      if (generate) refreshCustomList(generator);
    } else if (!generator.isCached(challenge)) {
      generator.addToCache(challenge);
      if (generate) refreshCustomList(generator);
    } else {
      generator.updateElementDisplay(challenge);
    }
  }

  private void refreshCustomList(IMenuGenerator generator) {
    // page count may have changed; regenerate the cached list pages for all known locales
    if (generator instanceof CustomHomeMenuGenerator home) {
      home.getListGenerator().updatePages();
    }
    Challenges.getInstance().getMenuManager().invalidateMainMenu();
  }

  public void executeTrigger(@NotNull ChallengeExecutionData challengeExecutionData) {
    // Copy: an action can register or unregister a custom challenge while it runs
    TriggerActionChallenge.dispatch(new ArrayList<>(customChallenges.values()), challengeExecutionData);
  }

  public boolean isTriggerEnabled(@NotNull IChallengeTrigger trigger) {
    return TriggerActionChallenge.isTriggerEnabled(customChallenges.values(), trigger);
  }

}
