package net.cyperia.challenges.plugin.challenges.custom.settings.trigger;

import net.cyperia.challenges.plugin.challenges.custom.TriggerActionChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public interface IChallengeTrigger extends Listener {

  default ChallengeExecutionData createData() {
    return new ChallengeExecutionData(this);
  }

  /**
   * Whether the trigger counts as met for this challenge. Overridden by a trigger that keeps its own
   * per-challenge state on top of the plain sub-trigger match, such as which chunks a player already
   * entered.
   */
  default boolean isTriggerMet(@NotNull TriggerActionChallenge challenge, @NotNull ChallengeExecutionData data) {
    return challenge.isTriggerMet(data);
  }

}
