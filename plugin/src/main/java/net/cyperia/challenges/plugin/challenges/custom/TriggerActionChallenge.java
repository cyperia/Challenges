package net.cyperia.challenges.plugin.challenges.custom;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.IChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A challenge that uses a registered trigger, a registered action, or both; the side it does not take
 * from the registry is its own code. Implement it on any challenge base.
 * <p>
 * This is an interface and not a base class because its implementations need different menu behaviour:
 * a custom challenge is a plain toggle, a preset may carry a value the player can tune.
 */
public interface TriggerActionChallenge extends IChallenge {

  @Nullable
  ChallengeTrigger getTrigger();

  @NotNull
  Map<String, String[]> getSubTriggers();

  @Nullable
  ChallengeAction getAction();

  @NotNull
  Map<String, String[]> getSubActions();

  static void dispatch(@NotNull Iterable<? extends TriggerActionChallenge> challenges,
                       @NotNull ChallengeExecutionData executionData) {
    for (TriggerActionChallenge challenge : challenges) {
      if (challenge.getTrigger() == executionData.getTrigger()) {
        challenge.onTriggerFulfilled(executionData);
      }
    }
  }

  /**
   * Whether any of them is enabled and listens to this trigger, for a trigger that has state to keep or
   * work to do only while somebody uses it.
   */
  static boolean isTriggerEnabled(@NotNull Iterable<? extends TriggerActionChallenge> challenges,
                                  @NotNull IChallengeTrigger trigger) {
    for (TriggerActionChallenge challenge : challenges) {
      if (challenge.getTrigger() == trigger && challenge.isEnabled()) return true;
    }
    return false;
  }

  default void onTriggerFulfilled(ChallengeExecutionData challengeExecutionData) {
    // Enabled first: a disabled challenge must not record chunk/state memory its trigger keeps on the side.
    if (isEnabled() && challengeExecutionData.getTrigger().isTriggerMet(this, challengeExecutionData)) {
      executeAction(challengeExecutionData);
    }
  }

  /**
   * @return if the trigger is met.
   * That is when every key in the subTriggers is contained by the data map and one or more value
   * of the lists are equal to one another.
   */
  default boolean isTriggerMet(ChallengeExecutionData data) {
    Map<String, List<String>> triggerData = data.getTriggerData();
    Map<String, String[]> subTriggers = getSubTriggers();
    if (!subTriggers.isEmpty()) {
      for (Map.Entry<String, String[]> entry : subTriggers.entrySet()) {
        if (!triggerData.containsKey(entry.getKey())) {
          return false;
        }
        List<String> list = triggerData.get(entry.getKey());

        boolean match = false;
        for (String value : entry.getValue()) {
          if (list.contains(value)) {
            match = true;
            break;
          }
        }

        if (!match) {
          return false;
        }
      }
    }
    return true;
  }

  default void executeAction(ChallengeExecutionData challengeExecutionData) {
    ChallengeAction action = getAction();
    if (action == null) return;
    // Async triggers (the interval) hand their actions to the next tick, as actions touch the world
    if (!Bukkit.isPrimaryThread()) {
      Regions.nextTick(Challenges.getInstance(), () -> action.execute(challengeExecutionData, getSubActions()));
      return;
    }
    action.execute(challengeExecutionData, getSubActions());
  }

}
