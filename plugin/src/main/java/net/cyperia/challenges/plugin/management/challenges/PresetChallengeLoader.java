package net.cyperia.challenges.plugin.management.challenges;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.catalog.Definition;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.TriggerActionChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.IChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.implementation.LockedChallenge;
import net.cyperia.challenges.plugin.challenges.implementation.LockedEntry;
import net.cyperia.challenges.plugin.challenges.implementation.preset.PresetChallenge;
import net.cyperia.challenges.plugin.challenges.implementation.preset.PresetModifierChallenge;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Challenges built from a trigger and an action instead of their own implementation.
 * They are registered and displayed like any other challenge; only the way they are defined differs.
 */
public final class PresetChallengeLoader extends ModuleChallengeLoader {

  private final List<TriggerActionChallenge> triggerActionChallenges = new LinkedList<>();

  public PresetChallengeLoader() {
    super(Challenges.getInstance());
  }

  public void executeTrigger(@NotNull ChallengeExecutionData challengeExecutionData) {
    // Copy: an action can register or unregister a challenge while it runs
    TriggerActionChallenge.dispatch(new ArrayList<>(triggerActionChallenges), challengeExecutionData);
  }

  public boolean isTriggerEnabled(@NotNull IChallengeTrigger trigger) {
    return TriggerActionChallenge.isTriggerEnabled(triggerActionChallenges, trigger);
  }

  /**
   * Adds a challenge to the dispatch list without registering it: for a {@link TriggerActionChallenge}
   * that is not a preset, such as one with its own coded menu, registered elsewhere.
   */
  void listen(@NotNull TriggerActionChallenge challenge) {
    triggerActionChallenges.add(challenge);
  }

  void unlisten(@NotNull TriggerActionChallenge challenge) {
    triggerActionChallenges.remove(challenge);
  }

  void preset(@NotNull Challenge entry, @NotNull Definition.Preset preset) {
    ChallengeTrigger trigger = Challenges.getInstance().getCustomSettingsLoader().getTriggerByName(preset.trigger());
    ChallengeAction action = Challenges.getInstance().getCustomSettingsLoader().getActionByName(preset.action());
    if (trigger == null || action == null) {
      Logger.error("Could not register preset challenge {}, unknown trigger '{}' or action '{}'", entry.id(), preset.trigger(), preset.action());
      return;
    }
    // The public release ships beta triggers and actions locked, and a preset built from one with them
    if (trigger instanceof LockedEntry || action instanceof LockedEntry) {
      register(new LockedChallenge(entry));
      return;
    }
    Map<String, String[]> subTriggers = settings(preset.triggerSettings());
    Map<String, String[]> subActions = settings(preset.actionSettings());
    Definition.Preset.Value value = preset.value();
    TriggerActionChallenge challenge = value == null
      ? new PresetChallenge(entry, trigger, subTriggers, action, subActions)
      : new PresetModifierChallenge(entry, valueUnit(value.unit()), value.min(), value.max(), value.defaultValue(),
      trigger, subTriggers, action, subActions);
    register(challenge);
  }

  @NotNull
  private static Map<String, String[]> settings(@NotNull Map<String, List<String>> settings) {
    return settings.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
      setting -> setting.getValue().toArray(String[]::new), (first, second) -> first, LinkedHashMap::new));
  }

  @NotNull
  private static ValueUnit valueUnit(@NotNull String constant) {
    try {
      return (ValueUnit) ValueUnit.class.getField(constant).get(null);
    } catch (ReflectiveOperationException ex) {
      throw new IllegalArgumentException("No ValueUnit " + constant, ex);
    }
  }

}
